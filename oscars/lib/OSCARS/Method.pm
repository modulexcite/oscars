#==============================================================================
package OSCARS::MethodFactory;

use strict;
use Data::Dumper;

use OSCARS::PluginManager;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}


sub initialize {
    my( $self ) = @_;

    $self->{pluginMgr} = OSCARS::PluginManager->new();
} #____________________________________________________________________________


###############################################################################
#
sub instantiate {
    my( $self, $user, $params, $logger ) = @_;

    my $className = $self->{pluginMgr}->getLocation($params->{method});
    my $dbname = $self->{pluginMgr}->getLocation('system');
    my $db = $user->getDbHandle($dbname);
    my $locationPrefix = $className;
    $locationPrefix =~ s/(::)/\//g;
    my $location = $locationPrefix . '.pm';
    require $location;
    # TODO:  shouldn't need so many parameters
    return $className->new( 'user' => $user,
	                     'db' => $db,
			     'database' => $dbname,
                             'params' => $params,
		             'logger' => $logger );
} #___________________________________________________________________________ 


#==============================================================================
package OSCARS::Method;

=head1 NAME

OSCARS::Method - Superclass for all SOAP methods.

=head1 SYNOPSIS

  use OSCARS::Method;

=head1 DESCRIPTION

Superclass for all SOAP methods.  Contains methods for all phases of
a SOAP request.  Assumes that authentication has already been performed.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 23, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Mail;
use OSCARS::Library::Reservation::ClientForward;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{forwarder} = OSCARS::Library::Reservation::ClientForward->new();
    $self->{mailer} = OSCARS::Mail->new();
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# authorized:  Check whether user calling this method has the proper 
#     authorizations, including viewing and setting parameters.  If not 
#     overriden, a noop.
#
sub authorized {
    my( $self ) = @_;

    return 1;
} #____________________________________________________________________________


###############################################################################
# validate:  validate incoming parameters
#
sub validate {
    my( $self ) = @_;

    my( $test );

    my $method = $self->{params}->{method};
    if ( !$method ) { return; }

    # for all tests 
    for my $testName (keys(%{$self->{paramTests}->{$method}})) {
        $test = $self->{paramTests}->{method}->{$testName};
        if (!$self->{params}->{$testName}) {
            throw Error::Simple(
                "Cannot validate $self->{params}->{method}, test $testName failed");
        }
        if ($self->{params}->{$testName} !~ $test->{regexp}) {
            throw Error::Simple( $test->{error} );
        }
    }
} #____________________________________________________________________________


###############################################################################
sub numericCompare {
    my( $self, $val, $lesser, $greater ) = @_;

    if ($lesser > $val) { return 0; }
    if ($greater < $val) { return 0; }
    return 1;
} #____________________________________________________________________________


###############################################################################
# dispatch
#
sub dispatch {
    my( $self ) = @_;

    return 1;
} #___________________________________________________________________________ 


###############################################################################
# postProcess:  Perform any operations necessary after making SOAP call
#
sub postProcess {
    my( $self, $results ) = @_;

    my $messages = $self->generateMessages($results);
    if ($messages) {
        $self->{mailer}->sendMessage($messages);
    }
} #___________________________________________________________________________ 


###############################################################################
# generateMessages:  overriden if anything to mail
#
sub generateMessages {
    my( $self, $results ) = @_;

    return undef;
} #___________________________________________________________________________ 


######
1;
