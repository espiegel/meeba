<?php
/**
 * Controller of all API requests
 * Accepts GET and POST
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
$db = new DB_Functions($app);

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

$app->get('/getUserByEmail/:email', function($email) use ($app, $db) {
	$user = $db->getUserByEmail($email);
	if(!$user) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Couldn't find user with email $email",
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

$app->get('/getUserByUid/:uid', function($uid) use ($app, $db) {
	$user = $db->getUserByUid($uid);
	if(!$user) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Couldn't find user with uid $uid",
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

$app->post('/createUser', function() use ($app, $db) {
	$email = $_POST['email'];
	$name = $_POST['name'];
	$phone = $_POST['phone'];
	$rid = $_POST['rid'];
	$picture_url = $_POST['picture_url'];
	$dummy = $_POST['is_dummy'];

	// First we check if a user exists by the same email or phone
	$user = $db->getUserByEmail($email); // check for user by email
	$user2 = $db->getUserByPhone($phone); // check for user by phone
	if ($user != false || $user2 != false) { 		
 		echo json_encode(array(
			'success' => 0,
			'error' => "A user already exists with the same " . (($user!=false)?"email":"phone"),
			'user' => null,
		));

		return;
	}

	// Otherwise store a new user
	$user = $db->createUser($email, $name, $rid, $phone, $picture_url, $dummy);
	if(!$user) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Couldn't create user",
			'user' => null
			));
	}

	echo json_encode(array(
		'success' => 1,
		'error' => null,
		'user' => $user
	));
});

$app->get('/getEventsByUser/:uid', function($uid) use ($app, $db) {
	$events = $db->getEventsByUser($uid);

	if(!$events) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to get events of user $uid",
			'events' => null,
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'events' => $events,
		));
});

$app->get('/getEventsByUser/:uid/:status', function($uid,$status) use ($app, $db) {
	$events = $db->getEventsByUser($uid,$status);

	if(!$events) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to get events of user $uid with status $status",
			'events' => null,
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'events' => $events,
		));
});

$app->post('/getUsersByPhones', function() use ($app, $db) {
	if(isset($_POST['phones'])) {
		$phones = $_POST['phones'];
		$app->getLog()->info("before phones = " . print_r($phones,TRUE));
		$phones = json_decode($phones); // decode the json array
		$app->getLog()->info("after phones = " . print_r($phones,TRUE));
		if($phones != null) {
			$users = $db->getUsersByPhones($phones);
		}
	}
	if(!isset($_POST['phones']) || !isset($users) || !$users) {
		$app->getLog()->info("Failed to get users from phone array");
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to get users from phone array",
			'users' => null,
		));

		return;
	}

	$app->getLog()->info("Success getting users! users = " . print_r($users, TRUE));
	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'users' => $users,
		));

});

$app->post('/createEvent', function() use ($app, $db) {
	$host_uid = $_POST['host_uid'];
	$where = $_POST['where'];
	$when = $_POST['when'];
	$uid = $_POST['uid']; // array of guest uids
	$title = $_POST['title'];

	$uid = json_decode($uid); // decode the json array

	// if the host is also invited then remove him from the guest array of uids
	// shouldn't happen normally

	// We actually don't need this for debugging!
	/*$key = array_search($host_uid, $uid);
	if($key != false) { 
		unset($uid[$key]);
		$uid = array_values($uid);
	}*/

	// Create the event and send out invites
	$event = $db->createEvent($host_uid, $title, $where, $when, $uid);

	if(!$event) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to create the event",
			'event' => null,
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'event' => $event,
		));	
	
});

$app->post('/respondToInvite', function() use ($app, $db) {
	$uid = $_POST['uid'];
	$eid = $_POST['eid'];
	$status = $_POST['status'];

	$result = $db->respondToInvite($uid, $eid, $status);

	if(!$result) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to respond to the invitation",
		));

		return;
	}

	echo json_encode(array(
		'success' => 1,
		'error' => null
	));
});

$app->get('/getUsersByEvent/:eid', function($eid) use ($app, $db) {
	$users = $db->getUsersByEvent($eid);

	if(!$users) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to get users by eid=$eid",
			'users' => null,
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'users' => $users,
		));
});

$app->get('/deleteEvent/:eid', function($eid) use ($app, $db) {
	$success = $db->deleteEvent($eid);

	if(!$success) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to delete event with eid=$eid",
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
		));
});

$app->get('/placeAutocomplete/:input', function($input) use ($app, $db) {
	$places = $db->placeAutocomplete($input);

	if($places == null) {	
		echo json_encode(array(
			'success' => 0,
			'places' => null,
			'error' => "Failed to get autocomplete",
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'places' => $places,
			'error' => null,
		));
});

$app->post('/updateEvent', function() use ($app, $db) {
	$eid = $_POST['eid'];
	$title = $_POST['title'];
	$when = $_POST['when'];
	$where = $_POST['where'];

	$event = $db->updateEvent($eid, $title, $when, $where);

	if(!$event) {
		echo json_encode(array(
			'success' => 0,
			'error' => "Failed to update the event",
			'event' => null,
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'error' => null,
			'event' => $event,
		));	
});

$app->post('/uploadEventPicture', function() use ($app, $db) {
	$eid = $_POST['eid'];
	$pictureData = $_POST['pictureData'];

	$url = $db->uploadEventPicture($eid, $pictureData);

	if(!$success) {
		echo json_encode(array(
			'success' => 0,
			'url' => null,
			'error' => "Failed to upload the picture",
		));

		return;
	}

	echo json_encode(array(
			'success' => 1,
			'url' => $url,
			'error' => null,
		));
});

$app->run();
