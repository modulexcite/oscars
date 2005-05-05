package BSS::Frontend::Reservation;

# Reservation.pm:
# Last modified: April 22, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use BSS::Frontend::Database;

use Data::Dumper;

######################################################################
sub new {
  my ($_class, %_args) = @_;
  my ($_self) = {%_args};
  
  # Bless $_self into designated class.
  bless($_self, $_class);
  
  # Initialize.
  $_self->initialize();
  
  return($_self);
}

######################################################################
sub initialize {
    my ($self) = @_;

    $self->{'dbconn'} = BSS::Frontend::Database->new('configs' => $self->{'configs'}) or die "FATAL:  could not connect to database";
}


################################
### insert_reservation
###
### Called from the scheduler to insert a row into the reservations table.
### Error checking has already been done by scheduler and CGI script.
###
### IN:  reference to hash.  Hash's keys are all the fields of the reservations
###      table except for the primary key.
### OUT: error status (0 success, 1 failure), and the results hash.
################################

sub insert_reservation
{
  my( $self, $inref ) = @_;
  my( $query, $sth );
  my( %results );

  my $over_limit = 0; # whether any time segment is over the bandwidth limit
  my( %table ) = $self->{'dbconn'}->get_BSS_table('reservations');

    ###
    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
  $query = "SELECT $table{'reservations'}{'bandwidth'}, $table{'reservations'}{'start_time'}, $table{'reservations'}{'end_time'} FROM reservations WHERE ( $table{'reservations'}{'end_time'} >= ? AND $table{'reservations'}{'start_time'} <= ? )";

  $inref->{'created_time'} = '';  # only holds a time if reservation successful

      # handled query with the comparison start & end datetime strings
  ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'reservations', $inref->{'start_time'}, $inref->{'end_time'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

  # TODO:  find segments of overlap, determine if bandwidth in any is
  #        over the limit; return time segments if error

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.

  #$results{'id'} unless ( $over_limit );
  if ( $over_limit ) {
      $self->{'dbconn'}->handle_finish( 'reservations');
          # TODO:  list of times
      results{'error_msg'} = '[ERROR] The available bandwidth limit on the network has been reached between '. 'Please modify your reservation request and try again.';
      return( 1, %results );
  }
  else {
      $self->{'dbconn'}->query_finish();
      $self->{'dbconn'}->unlock_table( 'reservations' );
      #print STDERR Dumper($inref);

      if (($inref->{'ingress_router'} == 0) || ($inref->{'egress_router'} == 0))
      {
          $results{'error_msg'} = 'Invalid router id(s): 0.  Unable to do insert.';
          return( 1, %results );
      }
          # get interface id's from edge router ip's
      $inref->{'ingress_id'} = $inref->{'ingress_router'}; 
      $inref->{'egress_id'} = $inref->{'egress_router'}; 

          # get ipaddr id from host's and destination's ip addresses
      $inref->{'src_id'} = $self->{'dbconn'}->hostaddr_to_idx($inref->{'src_ip'}); 
      $inref->{'dst_id'} = $self->{'dbconn'}->hostaddr_to_idx($inref->{'dst_ip'}); 
      $inref->{'created_time'} = time();

      my @insertions;   # copy over input fields that will be set in table
      my @resv_field_order = $self->{'dbconn'}->get_resv_field_order();
      foreach $_ ( @resv_field_order ) {
         $results{$_} = $inref->{$_};
         push(@insertions, $inref->{$_}); 
      }

        # insert all fields for reservation into database
      $query = "INSERT INTO reservations VALUES ( " . join( ', ', ('?') x @insertions ) . " )";

      ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'reservations', @insertions);
      if ( $results{'error_msg'} )
      {
          $results{'error_msg'} =~ "s/CantExecuteQuery\n//";
          $results{'error_msg'} = '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $results{'error_msg'};
           return( 1, %results );
      }
		
      $results{'id'} = $self->{'dbconn'}->get_id();
  }
  $self->{'dbconn'}->handle_finish( 'reservations' );
  $results{'status_msg'} = 'Your reservation has been processed successfully. Your reservation ID number is ' . $results{'id'} . '.';
  return( 0, %results );
}


# from reservationlist.pl:  Reservation List DB handling

##### sub get_reservations
### get the reservation list from the database and populate the table tag
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservations
{
  my( $self, $inref, $fields_to_read ) = @_;
  my( $sth, $query );
  my( %mapping, @field_names, $rref, $arrayref, $r, %results );

  my( %table ) = $self->{'dbconn'}->get_BSS_table('reservations');
    # DB query: get the reservation list
    # TODO:  selectall

  foreach $_ ( @$fields_to_read ) {
      push (@field_names, $table{'reservations'}{$_});
  }

  $query = "SELECT ";
  foreach $_ ( @field_names ) {
      $query .= $_ . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
    # sort by start time in ascending order
  $query .= " FROM reservations WHERE $table{'reservations'}{'dn'} = ? ORDER BY $table{'reservations'}{'start_time'}";

  ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'reservations', $inref->{'dn'});
  if ( $results{'error_msg'} ) { return(1, %results ); }

  $rref = $sth->fetchall_arrayref({user_dn => 1, reservation_start_time => 2, reservation_end_time => 3, reservation_status => 4, src_hostaddrs_id => 5, dst_hostaddrs_id => 6, reservation_id => 7 });
  $self->{'dbconn'}->query_finish();
  $self->{'dbconn'}->unlock_table( 'reservations' );

  $query = "SELECT hostaddrs_id, hostaddrs_ip FROM hostaddrs";
  ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'hostaddrs');
  if ( $results{'error_msg'} ) { return(1, %results ); }

  $arrayref = $sth->fetchall_arrayref();
  foreach $r (@$arrayref)
  {
      $mapping{$$r[0]} = $$r[1];
  }
  foreach $r (@$rref)
  {
      $r->{'src_hostaddrs_id'} = $mapping{$r->{'src_hostaddrs_id'}};
      $r->{'dst_hostaddrs_id'} = $mapping{$r->{'dst_hostaddrs_id'}};
  }
  $results{'rows'} = $rref;

  $self->{'dbconn'}->handle_finish( 'hostaddrs' );
  $results{'status_msg'} = 'Successfully read reservations';
  return( 0, %results );
}


    # stub
sub delete_reservation
{
}


##### sub get_reservation_detail
### get the reservation detail from the database
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservation_detail
{
  my( $self, $inref, $fields_to_display ) = @_;
  my( $sth, $query );
  my( %mapping, $r, $arrayref, %results );

  my( %table ) = $self->{'dbconn'}->get_BSS_table('reservations');

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @$fields_to_display ) {
      $query .= $table{'reservations'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM reservations WHERE $table{'reservations'}{'id'} = ?";
  ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'reservations', $inref->{'id'});
  #print STDERR "query: ", $query, " id: ", $inref->{'id'}, "\n";

  if ( $results{'error_msg'} ) { return(1, %results ); }

    # populate %results with the data fetched from the database
  @results{@$fields_to_display} = ();
  $sth->bind_columns( map { \$results{$_} } @$fields_to_display );
  $sth->fetch();
  $self->{'dbconn'}->query_finish();
  $self->{'dbconn'}->unlock_table( 'reservations' );

  $query = "SELECT hostaddrs_id, hostaddrs_ip FROM hostaddrs";
  ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'hostaddrs');
  if ( $results{'error_msg'} ) { return(1, %results ); }

  $arrayref = $sth->fetchall_arrayref();
  foreach $r (@$arrayref)
  {
      $mapping{$$r[0]} = $$r[1];
  }
  $results{'src_ip'} = $mapping{$results{'src_id'}};
  $results{'dst_ip'} = $mapping{$results{'dst_id'}};

  $self->{'dbconn'}->handle_finish( 'hostaddrs' );
  $results{'status_msg'} = 'Successfully got reservation details.';
  return (0, %results);
}

1;
