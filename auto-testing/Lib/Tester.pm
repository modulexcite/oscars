package Lib::Tester;
use base qw(Class::Accessor);
use Carp;
use English qw(-no_match_vars);
use Lib::OscarsClientUtils;
use Math::BigFloat;
use Params::Validate qw(:all);
use Lib::TopologyUtils;
use strict;
use warnings;


=head1 NAME

Tester
    
=head1 DESCRIPTION


=cut

our $Debugging = 0;


our $NAME = "Tester";


sub create 
{
	my $self = shift;
	my %params = @_;
	my $parser = new Lib::TopologyUtils;
	my $client = new Lib::OscarsClientUtils;
	my $yamlFile = $self->name . "_" . __get_rand() . ".yaml";
	my $logFile = $params{'testName'} . ".log";
	my $result;


	$Lib::OscarsClientUtils::Debugging = 0;

	my $name = $params{'testName'};
	my $src = $params{'src'}; 
	my $srcVlan = $params{'srcVlan'}; 
	my $dst = $params{'dst'}; 
	my $dstVlan = $params{'dstVlan'}; 
	my $count = exists($params{'count'}) ? $params{'count'} : 60;
	my $startTime = exists($params{'startTime'}) ? $params{'startTime'} : 'now';
	my $endTime = exists($params{'endTime'}) ? $params{'endTime'} : '+00:00:12';


	# Set parameters
	$client->writeYaml(yamlFile => $yamlFile, testName => $name, 
			src => $src, dst => $dst, srcVlan => $srcVlan, dstVlan => $dstVlan,
			startTime => $startTime, endTime => $endTime);
	# Run test
	$result = $client->create(yamlFile => $yamlFile, topology => $params{'topology'}, 
			logFile => $logFile, testName => $name, src => $src, 
			dst => $dst, result => $params{'expectedResult'}, count => $count);

	return $result;
}



sub single_test
{
	my $self = shift;
	my %params = @_;
	my $parser = new Lib::TopologyUtils;
	my $client = new Lib::OscarsClientUtils;
	my $yamlFile = $self->name . "_" . __get_rand() . ".yaml";
	my $logFile = $params{'testName'} . ".log";
	my $result;


	$Lib::OscarsClientUtils::Debugging = 0;

	my $name = $params{'testName'};
	my $src = $params{'src'}; 
	my $srcVlan = $params{'srcVlan'}; 
	my $dst = $params{'dst'}; 
	my $dstVlan = $params{'dstVlan'}; 
	my $count = exists($params{'count'}) ? $params{'count'} : 60;
	my $startTime = exists($params{'startTime'}) ? $params{'startTime'} : 'now';
	my $endTime = exists($params{'endTime'}) ? $params{'endTime'} : '+00:00:12';


	# Set parameters
	$client->writeYaml(yamlFile => $yamlFile, testName => $name, 
			src => $src, dst => $dst, srcVlan => $srcVlan, dstVlan => $dstVlan,
			startTime => $startTime, endTime => $endTime);
	# Run test
	$result = $client->createCancelRes(yamlFile => $yamlFile, topology => $params{'topology'}, 
			logFile => $logFile, testName => $name, src => $src, 
			dst => $dst, result => $params{'expectedResult'}, count => $count);

	return $result;
}


sub multi_test
{
	my $self = shift;
	my @arr = @_;
	my @children;
	my $len = $#arr + 1;
	my $sleepMax = 120;

	for (my $i = 0; $i < $len; $i++) {
		sleep(int(rand($sleepMax)));		
		my %params = %{$arr[$i]};
	
		my $pid = fork();
		if ($pid) {
			push (@children, $pid);
		} elsif ($pid == 0) {
			my $tester = new Lib::Tester;
			$tester->single_test(%params);
			exit 0;
		} else {
			die "$NAME couldn't fork: $!\n";
		}
	}
	
	foreach (@children) {
		my $pid = waitpid($_, 0);
	}
}


sub multi_create
{
	my $self = shift;
	my @arr = @_;
	my @children;
	my $len = $#arr + 1;
	my $sleepMax = 1;

	for (my $i = 0; $i < $len; $i++) {
		sleep(int(rand($sleepMax)));		
		my %params = %{$arr[$i]};
	
		my $pid = fork();
		if ($pid) {
			push (@children, $pid);
		} elsif ($pid == 0) {
			my $tester = new Lib::Tester;
			$tester->create(%params);
			exit 0;
		} else {
			die "$NAME couldn't fork: $!\n";
		}
	}
	
	foreach (@children) {
		my $pid = waitpid($_, 0);
	}
}


sub topology_test 
{
	my $self = shift;
	my %params = @_;
	my $parser = new Lib::TopologyUtils;
	my $client = new Lib::OscarsClientUtils;
	my $topologyFile = $params{'topology'};
	my $yamlFile = $self->name . "_$^T" . ".yaml";
	my $logFile = $params{'testName'} . ".log";
	my $result;

	$Lib::OscarsClientUtils::Debugging = 0;

	my $name = $params{'testName'};

	my @topology = $parser->parseV6(fileName => $params{'topology'});
	foreach my $src (@topology) {
		foreach my $dst (@topology) {

			# Set parameters
			$client->writeYaml(yamlFile => $yamlFile, testName => $name, 
					src => $src->{'linkId'}, dst => $dst->{'remoteLinkId'});

			my %src = $parser->parse_urn(urn => $src->{'linkId'});
			my %dst = $parser->parse_urn(urn => $dst->{'remoteLinkId'});
			if ($src{'domain'} eq $dst{'domain'} and
					$src{'node'} eq $dst{'node'} and
					$src{'link'} eq $dst{'link'}) {
				next;	
			}
			if ($src{'domain'} eq '*' and
					$src{'node'} eq '*' and
					$src{'link'} eq '*') {
				next;
			}
			if ($dst{'domain'} eq '*' and
					$dst{'node'} eq '*' and
					$dst{'link'} eq '*') {
				next;
			}

			# Run test
			$client->createCancelRes(yamlFile => $yamlFile, topology => $params{'topology'}, 
					logFile => $logFile, testName => $name, src => $src->{'linkId'}, 
					dst => $dst->{'remoteLinkId'}, result => $params{'expectedResult'});
		}
	}
	$result = "Debugging";
	return $result; 
}


sub __get_rand
{
	my ($foo, $bar) = split(/\./, rand());
	$bar;
}


sub new
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    bless($self);
    return $self;
}


sub name 
{
    my $self = shift;
    
    if ($Debugging) {
        carp "Debugging $NAME";
    }
    return $NAME;
}

1;

