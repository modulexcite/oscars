#==============================================================================
package OSCARS::WBUI::Method::UserAddForm;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

=head1 NAME

OSCARS::WBUI::Method::UserAddForm - Outputs HTML form for adding a user.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserAddForm;

=head1 DESCRIPTION

Outputs HTML form for adding a user (does not make a SOAP call).

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

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

    my $request = $self->SUPER::modifyParams();
    $self->{method} = 'InstitutionList';
    return $request;
} #____________________________________________________________________________


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'UserList';
} #___________________________________________________________________________ 


###############################################################################
# outputContent: print add user form.
#
sub outputContent {
    my( $self, $request, $results ) = @_;

    my $submitStr = "return submitForm(this, 'method=UserAdd;',
			                checkAddUser);";
    my $msg = "Add User Form";
    print( qq{
    <h3>Add a new user</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$submitStr">
    <p><input type='submit' value='ADD'></input></p>
    <table>
    <tbody>
    <tr>
      <td>Login Name</td>
      <td><input class='required' type='text' name='selectedUser' size='40'></input></td>
    </tr>
    } );
    my $response = {};
    $self->outputPasswordFields();
    # default selection
    $response->{institutionName} = 'Energy Sciences Network';
    $response->{institutionList} = $results;
    my $details = OSCARS::WBUI::Method::UserDetails->new();
    $details->output( $request, $response );
    print("</tbody></table></form>\n");
    return $msg;
} #___________________________________________________________________________ 
 

###############################################################################
# outputPasswordFields:  print rows having to do with passwords
#
sub outputPasswordFields {
    my( $self ) = @_;

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
