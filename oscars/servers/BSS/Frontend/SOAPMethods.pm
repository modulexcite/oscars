package BSS::Frontend::SOAPMethods;

# SOAP methods for BSS.
#
# Note that all authentication and authorization handling is assumed
# to have been previously done by AAAS.  Use caution if running the
# BSS on a separate machine from the one running the AAAS.
#
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)


use strict;

use Error qw(:try);
use Data::Dumper;

use BSS::Frontend::DBRequests;
use BSS::Frontend::Policy;
use BSS::Frontend::Stats;
use BSS::Traceroute::RouteHandler;

# until can get MySQL and views going
my @user_fields = ( 'reservation_id',
                    'user_dn',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_status',
                    'src_hostaddr_id',
                    'dst_hostaddr_id',
                    'reservation_tag');

my @detail_fields = ( 'reservation_id',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_created_time',
                    'reservation_time_zone',
                    'reservation_bandwidth',
                    'reservation_burst_limit',
                    'reservation_status',
                    'src_hostaddr_id',
                    'dst_hostaddr_id',
                    'reservation_description',
                    'reservation_src_port',
                    'reservation_dst_port',
                    'reservation_dscp',
                    'reservation_protocol',
                    'reservation_tag');

my @detail_admin_fields = ( 'ingress_interface_id',
                    'egress_interface_id',
                    'reservation_path',
                    'reservation_class');


###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{policy} = BSS::Frontend::Policy->new(
                       'dbconn' => $self->{dbconn});
    $self->{route_setup} = BSS::Traceroute::RouteHandler->new(
                                               'dbconn' => $self->{dbconn});
}
######


###############################################################################
# insert_reservation:  SOAP call to insert a row into the reservations table. 
#     BSS::Traceroute::RouteHandler is called to set up the route before
#     inserting a reservation in the database
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub insert_reservation {
    my( $self, $inref ) = @_;
    my( $duration_seconds );

    # inref fields having to do with traceroute modified in find_interface_ids
    $self->{route_setup}->find_interface_ids($inref);
    $self->{dbconn}->setup_times($inref, $self->get_infinite_time());
    ( $inref->{reservation_class}, $inref->{reservation_burst_limit} ) =
                    $self->{route_setup}->get_pss_fields();

    # convert requested bandwidth to bps
    $inref->{reservation_bandwidth} *= 1000000;
    $self->{policy}->check_oversubscribe($inref);

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $inref->{src_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{source_ip}); 
    $inref->{dst_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{destination_ip}); 

    my $results = $self->get_results($inref);
    return $results;
}
######

##############################################################################
# delete_reservation:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub delete_reservation {
    my( $self, $inref ) = @_;

    my $status =  $self->{dbconn}->update_status( $inref, 'precancel' );
    return $self->get_reservation_details($inref);
}
######

###############################################################################
# get_all_reservations: get all reservations from the database
#
# In: reference to hash of parameters
# Out: reservations if any, and status message
#
sub get_all_reservations {
    my( $self, $inref ) = @_;

    my $statement = "SELECT * FROM reservations" .
             " ORDER BY reservation_start_time";
    my $rows = $self->{dbconn}->do_query($statement);
    return $self->process_reservation_request($inref, $rows);
}
######

###############################################################################
# get_user_reservations: get all user's reservations from the database
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub get_user_reservations {
    my( $self, $inref ) = @_;

    my( $statement );

    # TODO:  fix authorization
    if ( $inref->{engr_permission} ) {
        $statement = "SELECT *";
    }
    else {
        $statement = "SELECT " . join(', ', @user_fields);
    }
    $statement .= " FROM reservations" .
              " WHERE user_dn = ?";
    $statement .= " ORDER BY reservation_start_time";
    my $rows = $self->{dbconn}->do_query($statement, $inref->{user_dn});
    return $self->process_reservation_request($inref, $rows);
}
######

###############################################################################
# get_reservation_details: get details for one reservation
#
# In: reference to hash of parameters
# Out: hash ref of results, status message
#
sub get_reservation_details {
    my( $self, $inref ) = @_;

    my( $statement );

        # TODO:  Fix authorization checks
    if ( !($inref->{engr_permission}) ) {
        $statement = "SELECT " . join(', ', @detail_fields);
    }
    else {
        $statement = "SELECT " .
                 join(', ', (@detail_fields, @detail_admin_fields));
    }
    $statement .= " FROM reservations" .
              " WHERE reservation_id = ?" .
              " ORDER BY reservation_start_time";
    my $rows = $self->{dbconn}->do_query($statement, $inref->{reservation_id});
    return $self->process_reservation_request($inref, $rows);
}
######

###############################################################################
# process_reservation_request: handle get reservation(s) query, and
#                              reformat results before sending back
#
sub process_reservation_request {
    my( $self, $inref, $rows ) = @_;

    my( $resv );

    # TODO:  fix authorization
    if ( $inref->{engr_permission} ) { 
        $self->{dbconn}->get_engr_fields($rows); 
    }
    for $resv ( @$rows ) {
        $self->{dbconn}->convert_times($resv);
        $self->{dbconn}->get_host_info($resv);
        $self->check_nulls($resv);
    }
    return $rows;
}
######

#################
# Private methods
#################

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $self, $dtime ) = @_;

    my @ymd = split(' ', $dtime);
    return $ymd[0];
}
######

###############################################################################
# get_infinite_time:  returns "infinite" time
#
sub get_infinite_time {
    my( $self ) = @_;

    return '2039-01-01 00:00:00';
}
######

###############################################################################
# get_results:  
#
sub get_results {
    my( $self, $inref ) = @_;

    # build fields to insert
    my $statement = "SHOW COLUMNS from reservations";
    my $rows = $self->{dbconn}->do_query( $statement );
    my @insertions;
    my $results = {}; 
    # TODO:  necessary to do insertions this way?
    for $_ ( @$rows ) {
       if ($inref->{$_->{Field}}) {
           $results->{$_->{Field}} = $inref->{$_->{Field}};
           push(@insertions, $inref->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    # insert all fields for reservation into database
    $statement = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{dbconn}->do_query($statement, @insertions);
    $results->{reservation_id} = $self->{dbconn}->get_primary_id();
    # copy over non-db fields
    $results->{source_host} = $inref->{source_host};
    $results->{destination_host} = $inref->{destination_host};
    # clean up NULL values
    $self->check_nulls($results);
    # convert times back to user's time zone for mail message
    $self->{dbconn}->convert_times($results);

    # set user-semi-readable tag
    $results->{reservation_tag} = $inref->{user_dn} . '.' .
        $self->get_time_str($inref->{reservation_start_time}) .  "-" .
        $results->{reservation_id};
    $statement = "UPDATE reservations SET reservation_tag = ?
                 WHERE reservation_id = ?";
    $unused = $self->{dbconn}->do_query($statement,
                      $results->{reservation_tag}, $results->{reservation_id});
    # get loopback fields if have engr privileges
    # TODO:  need authorization for these fields
    $self->{dbconn}->get_engr_fields($results); 
    $results->{reservation_tag} =~ s/@/../;
    $results->{reservation_status} = 'pending';
    return $results;
}
######

###############################################################################
# check_nulls:  
#
sub check_nulls {
    my( $self, $resv ) = @_ ;

    my( $resv );

    # clean up NULL values
    if (!$resv->{reservation_protocol} ||
        ($resv->{reservation_protocol} eq 'NULL')) {
        $resv->{reservation_protocol} = 'DEFAULT';
    }
    if (!$resv->{reservation_dscp} ||
        ($resv->{reservation_dscp} eq 'NU')) {
        $resv->{reservation_dscp} = 'DEFAULT';
    }
}
######

1;
# vim: et ts=4 sw=4
