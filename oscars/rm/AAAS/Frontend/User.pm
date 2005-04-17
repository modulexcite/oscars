package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with user forms.
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use lib '../..';

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(verify_login get_profile set_profile );

use DB;
use AAAS::Frontend::Database;

# TODO:  FIX
our $non_activated_user_level = -1;

# from login.pl:  login interaction with DB

##### sub verify_login
# In: reference to hash of parameters
# Out: status code, status message
sub verify_login
{
  my($hashref) = @_;
  my( $dbh, $sth, $query, $num_rows, %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # get the password from the database
  $query = "SELECT $Table_field{'users'}{'password'}, $Table_field{'users'}{'level'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $num_rows, $sth) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $hashref->{'loginname'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # check whether this person is a registered user
  my $password_matches = 0;
  if ( $num_rows == 0 ) {
        # this login name is not in the database
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      $results{'error_msg'} = 'Please check your login name and try again.';
      return( 1, %results );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq $non_activated_user_level ) {
                # this account is not authorized & activated yet
              db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
              $results{'error_msg'} = 'This account is not authorized or activated yet.';
              return( 1, %results );
          }
          elsif ( $$ref[0] eq  $hashref->{'password'} ) {
              $password_matches = 1;
          }
      }
  }
  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});

  if ( !$password_matches ) {
      $results{'error_msg'} = 'Please check your password and try again.';
  }
  $results{'status_msg'} = 'The user has successfully logged in.';
      # The first value is unused, but I can't get SOAP to send a correct
      # reply without it so far.
  return( 0, %results );
}


#### from logout.pl:  DB operations associated with logout, a noop right now

sub logout
{
  my( $dbh, $sth, $query, %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) {
      return( 1, %results );
  }
  database_disconnect( $dbh );
  results{'status_msg'} = 'Logged out';
  return ( 0, %results );
}


# from myprofile.pl:  Profile DB interaction

##### sub get_profile
# In: reference to hash of parameters
# Out: status code, status message
sub get_profile
{
  my($hashref) = @_;
  my( $dbh, $sth, $num_rows, $error_msg, $query, %results);

  ### get the user detail from the database and populate the profile form
  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # names of the fields to be displayed on the screen
  my @fields_to_display = ( 'last_name', 'first_name', 'dn', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_display ) {
      $query .= $Table_field{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $error_msg, $num_rows, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $hashref->{'loginname'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

  if ( $num_rows == 0 )
  {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      $results{'error_msg'} = 'No such user in the database';
      return( 1, %results );
  }

    # populate %results with the data fetched from the database
  @results{@fields_to_display} = ();
  $sth->bind_columns( map { \$results{$_} } @fields_to_display );
  $sth->fetch();

  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
#  foreach $key(sort keys %results)
#  {
#        $value = $results{$key};
#        print STDERR "$key -> $value\n";
#  }
  $results{'status_msg'} = 'Retrieved user profile';
  return ( 0, %results );
}


##### sub set_profile
# In: reference to hash of parameters
# Out: status code, status message
sub set_profile
{
  my ($hashref) = @_;
  my( $dbh, $sth, $query, %results, $read_only_level, @read_only_user_levels );
  my ($update_password, $encrypted_password, $error_code );   # TODO:  FIX

  ( $results{'error_message'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) {
      return( 1, %results );
  }

    # user level provisioning:  # if the user's level equals one of the
    #  read-only levels, don't give them access 
  $query = "SELECT $Table_field{'users'}{'level'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $hashref->{'loginname'} );
  if ( $results{'error_msg'}) { return( 1, %results ); }

  while ( my $ref = $sth->fetchrow_arrayref ) {
      foreach $read_only_level ( @read_only_user_levels ) {
          if ( $$ref[0] eq $read_only_level ) {
              db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
              $results{'error_msg'} = '[ERROR] Your user level (Lv. ' . $$ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.';
              return ( 1, %results );
          }
      }
  }

  query_finish( $sth );

    # Read the current user information from the database to decide which
    # fields are being updated.  'password' should always be the last entry
    #  of the array (important for later procedures)
  my @fields_to_read = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'password' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_read ) {
      $query .= $Table_field{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $hashref->{'loginname'} );
  if ( $results{'error_msg'}) { return( 1, %results ); }

    # populate %results with the data fetched from the database
  @results{@fields_to_read} = ();
  $sth->bind_columns( map { \$results{$_} } @fields_to_read );
  $sth->fetch();

    ### check the current password with the one in the database before
    ### proceeding
  if ( $results{'password'} ne $hashref->{'password_current'} ) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      return( 1, 'Please check the current password and try again.' );
  }
  query_finish( $sth );

    # determine which fields to update in the user profile table
    # @fields_to_update and @values_to_update should be an exact match
    my( @fields_to_update, @values_to_update );

    # if the password needs to be updated, add the new one to the fields/
    # values to update
  if ( $update_password ) {
      push( @fields_to_update, $Table_field{'users'}{'password'} );
      push( @values_to_update, $encrypted_password );
  }

    # Remove password from the update comparison list.  'password' is the
    # last element of the array; remove it from the array
  $#fields_to_read--;

    # compare the current & newly input user profile data and determine
    # which fields/values to update
  foreach $_ ( @fields_to_read ) {
      if ( $results{$_} ne $hashref->{$_} ) {
          push( @fields_to_update, $Table_field{'users'}{$_} );
          push( @values_to_update, $hashref->{$_} );
      }
  }

    # if there is nothing to update...
  if ( $#fields_to_update < 0 ) {
      database_unlock_table( $Table{'users'} );
      database_disconnect( $dbh );
      return( 1, 'There is no changed information to update.' );
  }

    # prepare the query for database update
  $query = "UPDATE $Table{'users'} SET ";
  foreach $_ ( 0 .. $#fields_to_update ) {
      $query .= $fields_to_update[$_] . " = ?, ";
  }
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, @values_to_update, $hashref->{'loginname'} );
  if ( $results{'error_msg'}) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      $results{'error_msg'} =~ s/CantExecuteQuery\n//;
      $results{'error_msg'} =  'An error has occurred while updating your account information.<br>[Error] ' . $error_code . $results{'error_msg'};
      return( 1, %results );
  }

  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
  $results{'status_msg'} = 'Your account information has been updated successfully.';
  return( 0, %results );
}


# activateaccount:  Account Activation DB methods

##### sub process_account_activation
# In: reference to hash of parameters
# Out: status code, status message
sub process_account_activation
{
  my( $hashref ) = @_;
  my( $dbh, $sth, $error_code, $query, $num_rows, %results );

  ($results{'error_message'}, $dbh ) = database_connect($Dbname);
  if ( $error_code ) { return (1, $error_code ); }

    # get the password from the database
  $query = "SELECT $Table_field{'users'}{'password'}, $Table_field{'users'}{'activation_key'}, $Table_field{'users'}{'pending_level'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return ( 1, $error_code );
  }

  ( $error_code, $num_rows ) = query_execute( $sth, $hashref->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return ( 1, $error_code );
  }

      # check whether this person is a registered user
  my $keys_match = 0;
  my( $pending_level, $non_match_error );

  if ( $num_rows == 0 ) {
        # this login name is not in the database
      database_disconnect( $dbh );
      return ( 1, 'Please check your login name and try again.' );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq '' ) {
              $non_match_error = 'This account has already been activated.';
          }
          elsif ( $$ref[0] ne $hashref->{'password'} ) {
              $non_match_error = 'Please check your password and try again.';
          }
          elsif ( $$ref[1] ne $hashref->{'activation_key'} ) {
              $non_match_error = 'Please check the activation key and try again.';
          }
          else {
              $keys_match = 1;
              $pending_level = $$ref[2];
          }
      }
  }
  query_finish( $sth );

      ### if the input password and the activation key matched against those
      ### in the database, activate the account
  if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
    $query = "UPDATE $Table{'users'} SET $Table_field{'users'}{'level'} = ?, $Table_field{'users'}{'pending_level'} = ?, $Table_field{'users'}{'activation_key'} = '' WHERE $Table_field{'users'}{'dn'} = ?";

      ( $error_code, $sth ) = query_prepare( $dbh, $query );
      if ( $error_code ) {
          database_disconnect( $dbh );
          return( 1, $error_code );
      }

      ( $error_code, undef ) = query_execute( $sth, $pending_level, '0', $hashref->{'loginname'} );
      if ( $error_code ) {
          database_disconnect( $dbh );
          return( 1, $error_code );
      }
      query_finish( $sth );
      database_disconnect( $dbh );
  }
  else {
      database_disconnect( $dbh );
      return( 1, $non_match_error );
  }
  return( 0, 'The user account <strong>' . $hashref->{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );
}


# register:  user account registration db

##### sub process_registration
# In:  reference to hash of parameters
# Out: status message
sub process_registration
{
  my( $hashref ) = @_;
  my $encrypted_password = $hashref->{'password_once'};

    # get current date/time string in GMT
  my $current_date_time = $hashref ->{'utc_seconds'};
  my( $dbh, $sth, $error_code, $query, $num_rows );
	
  ( $error_code, $dbh ) = database_connect($Dbname);
  if ( $error_code ) { return( 1, $error_code ); }

    # login name overlap check
  $query = "SELECT $Table_field{'users'}{'dn'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";
  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_rows ) = query_execute( $sth, $hashref->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }
  query_finish( $sth );

  if ( $num_rows > 0 ) {
      database_disconnect( $dbh );
      return( 0, 'The selected login name is already taken by someone else; please choose a different login name.' );
  }

  $query = "INSERT INTO $Table{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
    database_disconnect( $dbh );
    return( 1, $error_code );
  }

    # Initial user level is set to 0; needs admin accept/user activation to
    # raise the user level
  my @stuffs_to_insert = ( '', $hashref->{'loginname'}, $encrypted_password, $hashref->{'firstname'}, $hashref->{'lastname'}, $hashref->{'organization'}, $hashref->{'email_primary'}, $hashref->{'email_secondary'}, $hashref->{'phone_primary'}, $hashref->{'phone_secondary'}, $hashref->{'description'}, 0, $current_date_time, '', 0 );

  ( $error_code, undef ) = query_execute( $sth, @stuffs_to_insert );
  if ( $error_code ) {
      database_disconnect( $dbh );
      $error_code =~ s/CantExecuteQuery\n//;
      return( 1, 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $error_code );
  }
  query_finish( $sth );

  database_disconnect( $dbh );
  return( 0, 'Your user registration has been recorded successfully. Your login name is <strong>' . $hashref->{'loginname'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );
}

1;
