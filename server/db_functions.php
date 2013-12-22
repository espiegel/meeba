<?php
 
class DB_Functions {
 
    private $db;
 
    //put your code here
    // constructor
    function __construct() {
        require_once 'db_connect.php';

        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }
 
    // destructor
    function __destruct() {
 
    }
 
    /**
     * Storing new user
     * returns user details
     */
    public function createUser($email, $name, $rid, $phone, $picture_url) {
        // Escape special characters in the strings
        $email = mysql_real_escape_string($email);
        $name = mysql_real_escape_string($name);
        $phone = mysql_real_escape_string($phone);
        $picture_url = mysql_real_escape_string($picture_url);
        $rid = mysql_real_escape_string($rid);

    	$result = mysql_query("INSERT INTO users(phone_number, rid, created_at, email, name, picture_url) ".
            "VALUES('$phone', '$rid', NOW(), '$email', '$name', '$picture_url')");
    	if($result) {
    		$uid = mysql_insert_id();
    		$result = mysql_query("SELECT * FROM users WHERE uid = $uid");

    		return mysql_fetch_assoc($result);
    	} else {
    		return false;
    	}
    }
 
    /**
     * Get user by email
     */
    public function getUserByEmail($email) {
        $result = mysql_query("SELECT * FROM users WHERE email = '$email'") or die(mysql_error());
        // check for result
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            return mysql_fetch_assoc($result);
        } else {
            // user not found
            return false;
        }
    }

    /**
    * Get user by phone number
    */
    public function getUserByPhone($phone) {
    	$result = mysql_query("SELECT * FROM users WHERE phone_number = '$phone'") or die(mysql_error());

    	$no_of_rows = mysql_num_rows($result);
    	if ($no_of_rows > 0) {
            return mysql_fetch_assoc($result);
        } else {
            // user not found
            return false;
        }
    }

    /**
    * Get user by uid
    */
    public function getUserByUid($uid) {
        $result = mysql_query("SELECT * FROM users WHERE uid = $uid") or die(mysql_error());

        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            return mysql_fetch_assoc($result);
        } else {
            // user not found
            return false;
        }
    }

    /**
    * Get event by eid
    */
    public function getEventByEid($eid) {
        $result = mysql_query("SELECT * FROM events WHERE eid = $eid") or die(mysql_error());

        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            $response = mysql_fetch_assoc($result);

            $host_uid = $response['host_uid'];

            $host = mysql_fetch_assoc(mysql_query("SELECT * from `users` where uid = $host_uid"));

            $response['host'] = $host;
            return $response;
        } else {
            // user not found
            return false;
        }
    }

    /**
    * Get events of user by uid and by status
    */
    public function getEventsByUser($uid,$status = NULL) {
        // If no status is passed
        if($status == NULL) {
            $result = mysql_query("SELECT i.eid, e.created_at FROM `invites` i, `events` e WHERE guest_uid = $uid and i.eid=e.eid UNION ".
                              "SELECT eid,created_at FROM `events` WHERE host_uid = $uid ORDER BY created_at DESC") or die(mysql_error());
        }
        // If status was passed
         else {
            if($status == 0 || $status == -1) {
                $result = mysql_query("SELECT i.eid FROM `invites` i WHERE guest_uid = $uid AND invite_status = $status") or die(mysql_error());
            } else if($status != 1) {
                return false;
            } else {
                $result = mysql_query("SELECT i.eid, e.created_at FROM `invites` i, `events` e WHERE guest_uid = $uid AND ".
                    "invite_status = $status and i.eid=e.eid UNION ".
                    "SELECT eid,created_at FROM `events` WHERE host_uid = $uid ORDER BY created_at DESC") or die(mysql_error());
            }
        }
        $events = array();
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            $i = 0;
            while($row = mysql_fetch_array($result)) {
                $eid = $row['eid'];
                
                $eventdata = mysql_query("SELECT * from `events` WHERE eid = $eid");
                if(!$eventdata) {
                    continue;
                }           
                
                $data = mysql_fetch_assoc($eventdata);
                $hostuid = $data['host_uid'];                            

                // Check whether such an event exists otherwise continue
                $hostdata_query = mysql_query("SELECT * from `users` WHERE uid = $hostuid");
                if($hostdata_query == false) {
                    continue;
                }
                $hostdata = mysql_fetch_assoc($hostdata_query);

                $events[$i] = $data;
                //$events[$i]['eid'] = $eid;
                $events[$i]['host'] = $hostdata;
                //$events[$i]['where'] = $data['where'];
                //$events[$i]['when'] = $data['when'];
                //$events[$i]['created_at'] = $data['created_at'];

                $i++;
            }

            return $events;
        } else {
            return false;
        }    
    }

    /**
    * Get an array of users from an array of phones
    */
    public function getUsersByPhones($phones) {
        if($phones == null) {
            return false;
        }

        $phone_numbers = array();

        foreach($phones as $phone) {
            $phone = mysql_real_escape_string($phone);
            if(!empty($phone)) {
                if($this->startsWith($phone,"972")) {
                    $phone = substr_replace($phone, "0", 0, 3);
                }
                array_push($phone_numbers, $phone);
            }
        }

        $phoneList = implode("," , $phone_numbers);
        //$app = \Slim\Slim::getInstance();
        //$app->getLog()->info("phonelist = " . $phoneList);
        $sql = 'SELECT * FROM users WHERE phone_number IN ('. $phoneList .')';
        $result = mysql_query($sql) or die(mysql_error());
        
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            $users = array();

            $i=0;
            while($r = mysql_fetch_assoc($result)) {
                $users[$i] = $r;
                $i = $i + 1;
            }

            return $users;
        }
        else
            return false;    
    }

    /**
    * Get an array of users by a specific event
    */
    public function getUsersByEvent($eid) {
        // First get the host
        $host = mysql_fetch_assoc(mysql_query("SELECT * FROM `users` u,`events` e WHERE e.eid = $eid AND e.host_uid = u.uid"));

        $result = array();

        $result['host'] = $host;

        // Now lets get all the guests
        $guest_result = mysql_query("SELECT DISTINCT uid, email, name, phone_number, rid, created_at, invite_status, picture_url FROM ".
            "`users`,`invites` WHERE `invites`.eid = $eid AND `invites`.guest_uid = `users`.uid");

        $no_of_rows = mysql_num_rows($guest_result);
        if ($no_of_rows <= 0) {
            return false;
        }

        $i = 0;
        while($row = mysql_fetch_assoc($guest_result)) {
            $result['guests'][$i] = $row;
            $i++;
        }

        return $result;
    }

    /**
    * Create an event, create and send out invitations to guests via gcm
    */
    public function createEvent($host_uid, $title, $where, $when, $uid) {
        // Escape special characters inside the string
        $title = mysql_real_escape_string($title);
        $where = mysql_real_escape_string($where);
        $when = mysql_real_escape_string($when);

        $app = \Slim\Slim::getInstance();
        $app->getLog()->info("inside create_event");
        $app->getLog()->info("INSERT INTO events(`host_uid`, `title`, `where`, `when`, `created_at`) VALUES('$host_uid', '$title', '$where', '$when', NOW())");
        $result = mysql_query("INSERT INTO events(`host_uid`, `title`, `where`, `when`, `created_at`) VALUES('$host_uid', '$title', '$where', '$when', NOW())")
            or die(mysql_error());

        if(!$result) {
            return false;
        }

        $eid = mysql_insert_id();
        $app->getLog()->info("SELECT * FROM `events` WHERE eid = $eid");
        $event = mysql_query("SELECT * FROM `events` WHERE eid = $eid");
        $event = mysql_fetch_assoc($event);

        $app->getLog()->info("SELECT * from `users` WHERE uid = $host_uid");
        $host = mysql_fetch_assoc(mysql_query("SELECT * from `users` WHERE uid = $host_uid"));
        $event['host'] = $host;
        $app->getLog()->info("event = " . print_r($event, TRUE));

        // Lets get an array of rids
        $guests = array();
        $rids = array();
        foreach($uid as $guest) {
            $user = $this->getUserByUid($guest);

            array_push($guests, $user);
            array_push($rids, $user['rid']);
        }
        $app->getLog()->info("guests = ".print_r($guests, true)." , rids= ".print_r($rids, true));
        // Get the host of the event
        $host = $this->getUserByUid($host_uid);

        // Create invitations for all the guests
        $this->createInvite($eid, $guests);

        // Send out invitations to an array of guest rids via gcm
        $response = $this->sendInvite($host, $rids, $event);

        // Log the response
        $app->getLog()->info("response = $response, array response=".print_r($response, TRUE));

        return $event;
    }

    /**
    * Delete an event by its eid. Also delete all invites associated with it.
    */
    public function deleteEvent($eid) {
        $deleteEventResult = mysql_query("DELETE FROM `events` WHERE eid = $eid");
        if(!$deleteEventResult) {
            return false;
        }

        $deleteInvitesResult = mysql_query("DELETE FROM `invites` where eid = $eid");
        if(!$deleteInvitesResult) {
            return false;
        }

        return true;
    }

    public function updateEvent($eid, $title, $when, $where) {
        // Escape special characters inside the string
        $title = mysql_real_escape_string($title);
        $where = mysql_real_escape_string($where);
        $when = mysql_real_escape_string($when);

        $query = mysql_query("UPDATE `events` SET `title` = '$title', `where` = '$where', `when` = '$when' WHERE eid = $eid");

        // Update failure
        if(!$query) {
            return false;
        }

        $event = $this->getEventByEid($eid);

        return $event;
    }

    /**
    * Create an invite for an array of guests
    */
    private function createInvite($eid, $guests) {
        foreach ($guests as $guest) {
            $uid = $guest['uid'];
            $result = mysql_query("INSERT INTO invites(eid, guest_uid, invite_status) VALUES($eid, $uid, 0)") or die(mysql_error());

            if(!$result) {
               //$app->getLog()->info("Failed to create invite for user uid=$uid");
            }
        }
    }

    /**
    * Send an invitation (notification) to an array of "rid"s
    *
    */
    public function sendInvite($host, $rids, $event) {
        $app = \Slim\Slim::getInstance();
        $app->getLog()->info("inside sendInvite, host=$host, rids=".print_r($rids, TRUE).", event=$event");
        // Message to send      
        $apiKey = API_KEY;
        
        // @Todo
        // Later we will want to add the list of guest names in the notification
        $response = $this->sendNotification( 
            $apiKey, 
            $rids, //array($registrationId), 
            array(
                'tag' => 'invite',
                'event' => $event
                )
            );
        
        return $response;
    }

    public function respondToInvite($uid, $eid, $status) {
        // Status can only be 0, 1 or -1
        if($status != 0 && $status != 1 && $status != -1) {
            return FALSE;
        }

        $result = mysql_query("UPDATE invites SET invite_status=$status WHERE eid=$eid AND guest_uid=$uid");

        // Send notification back to the event host ...
        $event = $this->getEventByEid($eid);
        $where = $event['where'];
        $when = $event['when'];
        $hostUid = $event['host_uid'];

        // Get guest and user arrays
        $guest = $this->getUserByUid($uid);
        $host = $this->getUserByUid($hostUid);

        // Get the 'rid' of the host
        $host_rid = $host['rid'];

        $apiKey = API_KEY;
        $response = $this->sendNotification(
                $apiKey,
                array($host_rid), //array($registrationId),
                array('tag' => 'inviteResponse',                     
                      'event' => $event,
                      'user' => $guest,
                      'status' => $status
                      )
                ); 

        return $response;
    }

    /**
     * The following function will send a GCM notification using curl.
     * 
     * @param $apiKey       [string] The Browser API key string for your GCM account
     * @param $registrationIdsArray [array]  An array of registration ids to send this notification to
     * @param $messageData      [array]  An named array of data to send as the notification payload
     */
    private function sendNotification( $apiKey, $registrationIdsArray, $messageData )
    {   
        $app = \Slim\Slim::getInstance();
        $app->getLog()->info("inside sendNotification: apiKey=$apiKey, messageData=".print_r($messageData,TRUE));

        $headers = array("Content-Type:" . "application/json", "Authorization:" . "key=" . $apiKey);
        $data = array(
            'data' => $messageData,
            'registration_ids' => $registrationIdsArray
        );
     
        $ch = curl_init();
     
        curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers ); 
        curl_setopt( $ch, CURLOPT_URL, "https://android.googleapis.com/gcm/send" );
        curl_setopt( $ch, CURLOPT_SSL_VERIFYHOST, 0 );
        curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, 0 );
        curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
        curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode($data) );
     
        $response = curl_exec($ch);
        curl_close($ch);
     
        return $response;
    }

    private function startsWith($haystack, $needle) {
        return $needle === "" || strpos($haystack, $needle) === 0;
    }
    private function endsWith($haystack, $needle) {
        return $needle === "" || substr($haystack, -strlen($needle)) === $needle;
    }

    /**
    * Autocompletes an input string and gives out an array of autocomplete options
    */
    public function placeAutocomplete($input) {
        // Need to replace all spaces with %20

        $array = array(
            'sensor' => 'false',
            'key' => API_KEY,
            'input' => $input
            );
        $param = http_build_query($array);
        $url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" . $param;

        $json = file_get_contents($url);

        $data = json_decode($json,TRUE);

        if(array_key_exists('predictions', $data)) {
            $predictions = $data['predictions'];
            $placeList = array();
            foreach($predictions as $place) {
                if(array_key_exists('description', $place)) {
                    array_push($placeList, $place['description']);
                }
            }
            return $placeList;
        }
        return false;
    }
}
?>