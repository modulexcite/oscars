#==============================================================================
package OSCARS::Internal::Permission::Remove;

=head1 NAME

OSCARS::Internal::Permission::Remove - SOAP method to remove a permission

=head1 SYNOPSIS

  use OSCARS::Internal::Permission::Remove;

=head1 DESCRIPTION

This is an internal SOAP method.  It removes a permission from the permissions
table.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::AAA::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{lib} = OSCARS::Library::AAA::Common->new('db' => $self->{db});
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Removes a permission from the permissions table
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage permissions");
    }
    my $results = {};
    my $params = $self->{params};
    my $permissionId =
        $self->{lib}->idFromName('permission', $params->{permissionName});
    my $statement = 'DELETE FROM permissions WHERE id = ?';
    my $unused = $self->{db}->doQuery($statement, $permissionId);
    my $msg = "Removed permission named $params->{permissionName}";
    return $results;
} #____________________________________________________________________________


######
1;
