#==============================================================================
package OSCARS::WBUI::Runner;

=head1 NAME

OSCARS::WBUI::Runner - Makes SOAP call and formats results for output.

=head1 SYNOPSIS

  use OSCARS::WBUI::Runner;

=head1 DESCRIPTION

Uses SOAPAdapterFactory to create a SOAP method instance, makes the SOAP call,
and formats the results for output.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

February 22, 2006

=cut


use strict;

use CGI qw{:cgi};
use SOAP::Lite;
use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;

#______________________________________________________________________________


###############################################################################
#
sub run {
    my($soap_uri, $soap_proxy, $hop, $params);
    my ( $cgi, %soap_params );

    if ( @_ > 0 ) {
        shift @_;
        $soap_uri = shift @_;
        $soap_proxy = shift @_;
        $hop = shift @_;
        $params = shift @_;
        $cgi = CGI->new($params);
        $cgi->param(-name => 'source_host', -value => $hop);
       	if($cgi->param("method") ne 'Login') {
            $cgi->param(-name => 'method', -value => 'CreateReservation')
	}
    }
    else {
        $soap_uri = 'http://localhost:2000/OSCARS/Dispatcher';
        $soap_proxy = 'http://localhost:2000/Server';
        $cgi = CGI->new();
    }
    my $soap_server = SOAP::Lite
                          -> uri( $soap_uri )
                          -> proxy( $soap_proxy );
    my $factory = OSCARS::WBUI::SOAPAdapterFactory->new();
    my $adapter = $factory->instantiate($cgi);
    $adapter->handle_request($soap_server);
} #____________________________________________________________________________


######
1;

