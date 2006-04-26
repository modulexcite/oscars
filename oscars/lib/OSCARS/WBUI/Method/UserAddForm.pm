#==============================================================================
package OSCARS::WBUI::Method::UserAddForm;

=head1 NAME

OSCARS::WBUI::Method::UserAddForm - Outputs HTML form for adding a user.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserAddForm;

=head1 DESCRIPTION

Outputs HTML form for adding a user (does not make a SOAP call).

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 26, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::UserDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# modifyParams:  resets method name
#
sub modifyParams {
    my( $self ) = @_;

    my $params = $self->SUPER::modifyParams();
    $params->{method} = 'InstitutionList';
    return $params;
} #____________________________________________________________________________


###############################################################################
# postProcess:  Reset the method name so the correct tab is highlighted.
#
sub postProcess {
    my( $self, $params, $results ) = @_;

    $params->{method} = 'UserList';
} #___________________________________________________________________________ 


###############################################################################
# outputDiv: print add user form.
#
sub outputDiv {
    my( $self, $results, $authorizations ) = @_;

    my $submitStr = "return submit_form(this, 'method=UserAdd;',
			                check_add_user);";
    my $msg = "Add User Form";
    print( qq{
    <div>
    <h3>Add a new user</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$submitStr">
    <table>
    <tbody>
    <tr>
      <td>Login Name</td>
      <td><input class='required' type='text' name='selectedUser' size='40'></input></td>
    </tr>
    } );
    $self->outputPasswordFields($results);
    # default selection
    $results->{institutionName} = 'Energy Sciences Network';
    my $details = OSCARS::WBUI::Method::UserDetails->new();
    $details->output( $results );
    return $msg;
} #___________________________________________________________________________ 
 

###############################################################################
# outputPasswordFields:  print rows having to do with passwords
#
sub outputPasswordFields {
    my( $self, $params ) = @_;

    print( qq{
    <tr>
      <td>Password (Enter twice)</td>
      <td><input class='required' type='password' name='passwordNewOnce' 
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Password Confirmation</td>
      <td><input class='required' type='password' name='passwordNewTwice' 
           size='40'></input>
      </td>
    </tr>
    } );
} #____________________________________________________________________________
 

######
1;
