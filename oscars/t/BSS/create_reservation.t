#!/usr/bin/perl -w

use strict;
use Data::Dumper;

use SOAP::Lite;

my( %params );

$params{reservation_start_time} = time();
$params{duration_hour} =       0.04;    # duration 5 minutes
$params{reservation_time_zone} = "-08:00";

# in Mbps
$params{reservation_bandwidth} =      '10';
$params{reservation_protocol} =       'udp';

$params{source_host} = 'nettrash3.es.net';
$params{destination_host} = 'dc-cr1.es.net';

$params{user_dn} =        'dwrobertson@lbl.gov';
$params{reservation_description} =    'This is a test.';
$params{server_name} = 'BSS';
$params{method} = 'create_reservation'; 

my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print STDERR Dumper($results);
print STDERR "Your reservation has been processed successfully. Your " .
   "reservation ID number is $results->{reservation_id}.\n";
