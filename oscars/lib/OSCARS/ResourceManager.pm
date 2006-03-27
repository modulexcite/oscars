# =============================================================================
package OSCARS::ResourceManager;

=head1 NAME

OSCARS::ResourceManager - resource manager for OSCARS.

=head1 SYNOPSIS

  use OSCARS::ResourceManager;

=head1 DESCRIPTION

Handles resources and authentication styles associated with OSCARS.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

March 24, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

use SOAP::Lite;

use OSCARS::Database;
use OSCARS::Logger;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{clients} = {};
    $self->{logger} = OSCARS::Logger->new();
} #____________________________________________________________________________


###############################################################################
# add_client:  Adds SOAP::Lite client for given OSCARS/BRUW domain to clients 
#              hash, indexed by autonomous system number.
#
sub add_client {
    my( $self, $as_num ) = @_;

    my( $statement, $client );

    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    # currently only handles one OSCARS server per domain
    if ($as_num) {
        $statement = 'SELECT * FROM clients WHERE as_num = ?';
        $client = $dbconn->get_row($statement, $as_num);
    }
    else {
	# local domain not given an AS number in the clients table
        $statement = 'SELECT * FROM clients WHERE as_num IS NULL';
	$as_num = 'local';
        $client = $dbconn->get_row($statement);
    }
    $dbconn->disconnect();
    if (!$client) { return undef; }
    $self->{clients}->{$as_num} = SOAP::Lite
                                        -> uri($client->{client_uri})
                                        -> proxy($client->{client_proxy});
    return $self->{clients}->{$as_num};
} #____________________________________________________________________________


###############################################################################
# has_client:  See if there exists a client to forward to another machine.
#
sub has_client {
    my( $self, $as_num ) = @_;

    if ($self->{clients}->{$as_num}) { return 1; }
    else { return 0; }
} #____________________________________________________________________________


###############################################################################
# forward:  Dispatch to server on another machine.
#
sub forward {
    my( $self, $as_num, $params ) = @_;

    my $som;
    if ( $self->{clients}->{$as_num} ) {
        $som = $self->{clients}->{$as_num}->dispatch($params);
    }
    else {
        $self->{logger}->add_string('Unable to forward; no such server');
        $self->{logger}->write_file('manager', $params->{method}, 1);
    }
    return $som;
} #____________________________________________________________________________


###############################################################################
# set_authentication_style:  Set current authentication style to given package.
#
sub set_authentication_style {
    my( $self, $package_name, $database ) = @_;

    my $location = $package_name . '.pm';
    $location =~ s/(::)/\//g;
    eval { require $location };
    # overwrites any previous authentication style
    if (!$@) {
        $self->{authN} = $package_name->new('database' => $database);
	return 1;
    }
    else { return 0; }
} #____________________________________________________________________________


###############################################################################
# authenticate:  Attempts to authenticate user.  Currently will only succeed
#    with Login method.
#
sub authenticate {
    my( $self, $daemon, $params ) = @_;

    my $user;
    $params->{database}= $self->{database};

    if ( $self->{authN} ) {
        $user = $self->{authN}->authenticate($daemon, $params);
    }
    return $user;
} #___________________________________________________________________________ 


###############################################################################
# write_exception:  Write exception to log.
#
sub write_exception {
    my( $self, $exception_text, $method_name ) = @_;

    $self->{logger}->add_string($exception_text);
    $self->{logger}->write_file('manager', $method_name, 1);
} #___________________________________________________________________________ 


###############################################################################
# Only used by OSCARS tests.
#
sub get_test_account {
    my( $self, $role ) = @_;

    my $statement = 'SELECT user_login, user_password FROM users ' .
                    'WHERE user_login = ?';
    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    my $results = $dbconn->get_row($statement, $role);
    $dbconn->disconnect();
    return( $results->{user_login}, $results->{user_password} );
} #____________________________________________________________________________


######
1;
