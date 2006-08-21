#==============================================================================
package OSCARS::Library::AAA::Common;

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

OSCARS::Library::AAA::Common - library for permissions, resources, auths

=head1 SYNOPSIS

  use OSCARS::Library::AAA::Common;

=head1 DESCRIPTION

Common library for operations on permissions, resources, resourcePermissions,
and authorizations tables.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $self into designated class.
    bless($self, $class);
    return($self);
} #____________________________________________________________________________


###############################################################################
# addRow:  Add a row to the AAA resources, permissions, or
#          resourcePermissions tables.  Not currently working.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub addRow {
    my( $self, $params, $tableName ) = @_;

    my $results = {};
    my $fields = $self->buildFields($self->{params});
    my $statement = "INSERT INTO foo VALUES(" .
                     join(', ', @$fields) . ")";
    $self->{db}->execStatement($statement);
    return;
} #____________________________________________________________________________


###############################################################################
#
sub buildFields {
    my( $self, $params ) = @_;

    my @fields = ();
    return \@fields;
} #____________________________________________________________________________


sub getResourcePermissions {
    my( $self, $params ) = @_;

    my( $resourceName, $permissionName, $auxResult );
    my $statement = "SELECT resourceId, permissionId " .
                    "FROM resourcePermissions";
    my $resourcePermissions = {};
    my $rpResults = $self->{db}->doSelect($statement);
    $statement = "SELECT name FROM resources WHERE id = ?";
    my $pstatement = "SELECT name FROM permissions WHERE id = ?";
    for my $row (@$rpResults) {
        $auxResult = $self->{db}->getRow($statement, $row->{resourceId});
        $resourceName = $auxResult->{name};
        if ( !$resourcePermissions->{$resourceName} ) {
            $resourcePermissions->{$resourceName} = {};
        }
        $auxResult = $self->{db}->getRow($pstatement, $row->{permissionId});
        $permissionName = $auxResult->{name};
        $resourcePermissions->{$resourceName}->{$permissionName} = 1;
    }
    return $resourcePermissions;
} #____________________________________________________________________________


######
1;
