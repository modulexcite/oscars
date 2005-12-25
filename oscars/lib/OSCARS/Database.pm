#==============================================================================
package OSCARS::Database;

=head1 NAME

OSCARS::Database - Handles database connection and basic database requests.

=head1 SYNOPSIS

  use OSCARS::Database;

=head1 DESCRIPTION

This module contains methods for handling database connections and queries.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

December 20, 2005

=cut


use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 


###############################################################################
#
sub connect {
    my( $self, $database_name ) = @_;

    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );

    # Temporary kludge; 12-24-2005
    if ($database_name eq 'BSS') { $database_name = 'BSSTest'; }
    # I couldn't find a foolproof way to check for timeout; Apache::DBI
    # came closest, but it was too dependent on the driver handling the timeout
    # correctly.  So instead,
    # if a handle is left over from a previous session, attempts to disconnect.
    # If it was timed out, the error is ignored.
    # TODO:  FIX
    if ($self->{dbh}) {
        $self->{dbh}->disconnect();
    }
    $self->{database} = 'DBI:mysql:' . $database_name;
    $self->{dbh} = DBI->connect(
                 $self->{database}, 
                 $self->{login}, 
                 $self->{password},
                 \%attr);
    if (!$self->{dbh}) {
        throw Error::Simple( "Unable to make database connection: $DBI::errstr");
    }
} #____________________________________________________________________________ 


###############################################################################
#
sub do_query {
    my( $self, $statement, @args ) = @_;

    # TODO, FIX:  selectall_arrayref probably better
    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    my $rows = $sth->fetchall_arrayref({});
    #if ( $DBI::err ) {
        #throw Error::Simple("[DBERROR] Fetching results of $statement:  $DBI::errstr");
    #}
    return $rows;
} #____________________________________________________________________________ 


###############################################################################
#
sub get_row {
    my( $self, $statement, @args ) = @_;

    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    my $rows = $sth->fetchall_arrayref({});
    if ( !@$rows ) { return undef; }
    # TODO:  error checking if more than one row
    return $rows->[0];
} #____________________________________________________________________________


###############################################################################
#
sub get_primary_id {
    my( $self ) = @_;

    return $self->{dbh}->{mysql_insertid};
} #____________________________________________________________________________


######
1;
