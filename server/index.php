<?php
/**
 * File to handle all API requests
 * Accepts GET and POST
 *
 * Each request will be identified by TAG
 * Response will be JSON data
 * check for POST request
*/
require 'vendor/autoload.php';
require 'DateTimeFileWriter.php';

$app = new \Slim\Slim();
$log = $app->getLog();
$log->setEnabled(true);
$log->setLevel(\Slim\Log::DEBUG);

$log->setWriter(new DateTimeFileWriter(array(
        'path' => './logs',
        'name_format' => 'Y-m-d',
        'message_format' => '%label% - %date% - %message%'
    )));
$writer = $log->getWriter();

// include db handler
require_once 'db_functions.php';
$db = new DB_Functions();

$app->post('/login', function() use ($app, $db) {
	$email = $_POST['email'];
    $name = $_POST['name'];
    $rid = $_POST['rid'];
    $phone = $_POST['phone'];
    // TODO: Add google access token
		
	$user = $db->getUserByEmail($email); // check for user by email
	$user2 = $db->getUserByPhone($phone); // check for user by phone
	if ($user != false || $user2 != false) { 		
 		echo json_encode(array(
			'success' => 1,
			'error' => null,
			'new' => 0,
			'user' => (($user!=false)?$user:$user2)
		));

		return;
	}

	// Otherwise store a new user
	$user = $db->createUser($email, $name, $rid, $phone);
	if(!$user) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Couldn't create user",
			'new' => 0,
			'user' => null
			));
	}

	echo json_encode(array(
		'success' => 1,
		'error' => null,
		'new' => 1,
		'user' => $user
	));
});

$app->get('/getUserByPhone/:phone', function($phone) use ($app, $db) {
	$user = $db->getUserByPhone($phone);
	if(!$user) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Couldn't find user with phone number $phone",
			'user' => null
			));	
		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'user' => $user
			));
});

$app->run();
