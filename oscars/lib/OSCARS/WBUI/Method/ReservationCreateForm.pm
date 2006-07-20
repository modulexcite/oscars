#==============================================================================
package OSCARS::WBUI::Method::ReservationCreateForm;

=head1 NAME

OSCARS::WBUI::Method::ReservationCreateForm - outputs reservation request form

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ReservationCreateForm;

=head1 DESCRIPTION

Handles engineer's request to display the create reservation form.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut

use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'ReservationCreateForm';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:   accessible from the "Create Reservation" notebook tab.
#    Prints out the reservation creation form.  Note that ingressRouterIP and 
#    egressRouterIP can be either a host name or an IP address.
# In:   hashes for request, response
# Out:  message string
#
sub outputContent {
    my( $self, $request, $response ) = @_;

    my $msg = "Reservation creation form";
    print( qq{
    <form method='post' action='' onsubmit="return submitForm(this, 
               'method=CreateReservation;', checkReservation);">
      <input type='hidden' class='SOAP' name='startTime'></input>
      <input type='hidden' class='SOAP' name='endTime'></input>
      <input type='hidden' class='SOAP' name='login' value='$request->{login}'></input>
      <input type='submit' value='Reserve bandwidth'></input>
      <input type='reset' value='Reset form fields'></input>

      <p>Required inputs are bordered in green.  Ranges or types of valid 
      entries are given in parentheses after the default values, if any. 
      If date and time fields are left blank, they are filled in with the 
      defaults.  The default time zone is your local time.</p>
    } );

    if ( $response->{loopbacksAllowed} ) {
        print( qq{
          <p><strong>WARNING</strong>:  Entering a value in a red-outlined field 
	  may change default routing behavior for the selected flow.</p> } );
    }
    print( qq{
    <table>
      <tbody>
      <tr><td>Source</td>
        <td class='required'>
          <input type='text' class='SOAP' name='srcHost' size='40'></input></td>
        <td>(Host name or IP address)</td></tr>
      <tr><td>Source port</td>
        <td><input type='text' class='SOAP' name='srcPort' maxlength='5' size='40'> 
             </input>
        </td>
	<td>(1024-65535)</td></tr>
      <tr><td>Destination</td>
        <td class='required'>
          <input type='text' class='SOAP' name='destHost' size='40'></input></td>
        <td>(Host name or IP address)</td></tr>
      <tr><td>Destination port</td>
        <td><input type='text' class='SOAP' name='destPort' maxlength='5' size='40'>
	    </input></td>
	<td>(1024-65535)</td></tr>
      <tr><td>Bandwidth (Mbps)</td>
        <td class='required'>
          <input type='text' class='SOAP' name='bandwidth' maxlength='7' size='40'>
          </input>
	</td>
        <td>(10-10000)</td></tr>
      <tr><td>Protocol</td>
        <td><input type='text' class='SOAP' name='protocol' size='40'></input></td>
	<td>(0-255, or string)</td></tr>
      <tr><td>Differentiated service code point</td>
        <td><input type='text' class='SOAP' name='dscp' maxlength='2' size='40'>
	    </input></td>
	<td>(0-63)</td></tr>
      <tr><td>Purpose of reservation</td>
        <td class='required'>
	    <input type='text' class='SOAP' name='description' size='40'></input></td>
        <td>(For our records)</td></tr>
    } );

    if ( $response->{loopbacksAllowed} ) {
      print( qq{
      <tr>
        <td>Ingress loopback</td>
        <td class='warning'>
	    <input type='text' class='SOAP' name='ingressRouterIP' size='40'></input>
        </td>
	<td>(Host name or IP address)</td>
	</tr>
      <tr>
        <td>Egress loopback</td>
        <td class='warning'>
	    <input type='text' class='SOAP' name='egressRouterIP' size='40'></input>
        </td>
	<td>(Host name or IP address)</td>
      </tr>
      } );
    }

    my @localSettings = localtime();
    my $year = $localSettings[5] + 1900;
    my $month = $localSettings[4] + 1;

    my $date = $localSettings[3];
    my $hour = $localSettings[2];
    my $minute = $localSettings[1];
    print( qq{
      <tr><td>Year</td>
        <td><input type='text' name='startYear' maxlength='4' size='40'></input></td>
        <td>$year</td></tr>
      <tr><td>Month</td>
        <td><input type='text' name='startMonth' maxlength='2' size='40'></input></td>
	<td>$month (1-12)</td></tr>
	<tr><td>Date</td>
        <td><input type='text' name='startDate' maxlength='2' size='40'></input></td>
	<td>$date (1-31)</td></tr>

      <tr><td>UTC offset</td>
        <td id="time-zone-options"> </td>
	<td id="local-time-zone"> </td>
      </tr>

      <tr><td>Hour</td>
        <td><input type='text' name='startHour' maxlength='2' size='40'></input></td>
	<td>$hour (0-23)</td></tr>
      <tr><td>Minute</td>
        <td><input type='text' name='startMinute' maxlength='2' size='40'></input></td>
	<td>$minute (0-59)</td></tr>
      <tr><td>Duration (Hours)</td>
        <td><input type='text' name='durationHour' maxlength='16' size='40'></input>
       	</td>
	<td>0.01 (0.01 to 4 years)</td></tr>
    } );
    if ( $response->{persistentAllowed} ) {
        print( qq{
      <tr><td>Persistent reservation</td>
        <td><input type='checkbox' name='persistent' size='8' value='0'></input></td>
	<td>Doesn't expire until explicitly cancelled.</td></tr> } );
    }
    print("</tbody></table></form>\n");
    return $msg;
} #____________________________________________________________________________


######
1;
