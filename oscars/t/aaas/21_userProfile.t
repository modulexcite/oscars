#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'AAAS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->set_authentication_style('OSCARS::AAAS::AuthN', 'AAAS');

my( $login, $password ) = $rm->get_test_account('testaccount');

my ($status, $msg) = UserProfile($login, $password);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub UserProfile {
    my( $user_login, $password ) = @_;

    my %params = ('user_login' => $user_login, 'user_password' => $password );

    $params{server} = $component_name;
    $params{method} = 'UserProfile';
    $params{op} = 'viewProfile';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved user profile\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
