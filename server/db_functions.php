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
    public function createUser($email, $name, $rid, $phone) {
    	$result = mysql_query("INSERT INTO users(phone_number, rid, created_at, email, name) VALUES('$phone', '$rid', NOW(), '$email', '$name')");
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
	
}
 
?>