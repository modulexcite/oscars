###############################################################################
package OSCARS::BSS::Method::ViewDetails;

=head1 NAME

OSCARS::BSS::Method::ViewDetails - SOAP method to view the details of a
specific reservation.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::ViewDetails;

=head1 DESCRIPTION

SOAP method that returns the details of an OSCARS reservation, given its
database id, from the reservations table in the BSS database.
It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

December 21, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::BSS::ReservationCommon;
use OSCARS::BSS::TimeConversionCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{resv_methods} = OSCARS::BSS::ReservationCommon->new(
                                                     'user' => $self->{user});
    $self->{time_methods} = OSCARS::BSS::TimeConversionCommon->new(
                                                     'user' => $self->{user});
} #____________________________________________________________________________


###############################################################################
# soap_method:  get reservation details from the database, given its
#     reservation id.  If a user has engr privileges, they can view any 
#     reservation's details.  Otherwise they can only view reservations that
#     they have made, with less of the details.
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub soap_method {
    my( $self ) = @_;

    return $self->{resv_methods}->view_details();
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
