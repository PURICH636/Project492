<?php
require_once("include/radius.class.php");

class DbFunction {

    public function __construct(){
        $DB_HOST = '';
        $DB_USER = '';
        $DB_PASS = '';
        $DB_NAME = '';
        $this->connection = $this->connect($DB_HOST, $DB_USER, $DB_PASS, $DB_NAME);
    }

    private function connect($host, $user, $pass, $db){
        
        $mysqli = mysqli_connect($host, $user, $pass, $db);
        if(mysqli_connect_error($mysqli))
            throw new Exception('Connection Error: '.$mysqli->connect_error);

        return $mysqli;
    }

    function checkLogin($username, $password){

        $email = strtolower($username);
        $tmp = explode('@',$email);
        $user = $tmp[0].'@'.$tmp[count($tmp)-1];

        if(strlen($user) > 1 && strlen($password) > 1){
            $radius = new Radius('authen.cm.edu','monitor1234','',2,1812,1813);

            if ($radius->AccessRequest($user,$password)){
                $sql_appkey = mysqli_query($this->connection, "SELECT `username`, `key` FROM `appKey` WHERE `username` = '$username';");
                $row_appkey = mysqli_fetch_assoc($sql_appkey);
                if(($row_appkey['username'] == NULL) && ($row_appkey['key'] == NULL)){
                    $apiKey = DbFunction::generateApiKey($password);
                    $rows_in = mysqli_query($this->connection, "INSERT INTO `appKey`(`username`, `key`) VALUES ('$username', '$apiKey');");
                    $rows_sql = mysqli_query($this->connection, "SELECT `key` FROM `appKey`;");
                    $result = mysqli_fetch_array($rows_sql);
                    return $result;
                }
                else{
                    $apiKey = DbFunction::generateApiKey($password);
                    $rows_in = mysqli_query($this->connection, "UPDATE `appKey` SET `key`='$apiKey' WHERE `username` = '$username';");
                    $rows_sql = mysqli_query($this->connection, "SELECT `key` FROM `appKey` WHERE `username` = '$username';");
                    $result = mysqli_fetch_array($rows_sql);
                    return $result;
                }
            }
            else{
                return false;
            }
        }
        mysqli_set_charset($this->connection,"utf8");
        mysqli_close($this->connection);
    }

    function generateApiKey($pass) {
        return sha1($pass);
    }

    function createList($username, $key){
        $response = array();  // array for JSON response
        $sql = mysqli_query($this->connection, "SELECT mac.username, appKey.key, mac.macaddress, mac.description  FROM `mac` INNER JOIN `appKey` ON mac.username = appKey.username WHERE mac.username = '$username' AND appKey.key = '$key';");
        mysqli_set_charset($this->connection,"utf8");
        if(mysqli_num_rows($sql) > 0){
            while($rlt = mysqli_fetch_array($sql,MYSQL_ASSOC)){
                $arr3 = array("description"=>$rlt['description'], "macaddress"=>$rlt['macaddress']);
                $arr4[] = $arr3;
            }
            $response = array("error"=>false, "username"=>$username, "data"=>$arr4); 
            echo json_encode($response);  
        }
        else{
            $response["error"]=true;
            echo json_encode($response);
        }
        
        mysqli_close($this->connection);
    }

    function editName($username, $key, $macaddress, $description){
        $sql_key = mysqli_query($this->connection, "SELECT `username`, `key` FROM `appKey` WHERE `username` = '$username' AND `key` = '$key';");
        $response = array(); 
        while ($row_key = mysqli_fetch_array($sql_key, MYSQL_ASSOC)) {
            if(($row_key['username'] == $username) && ($row_key['key'] == $key)){
                mysqli_query($this->connection, "UPDATE `mac` SET `description` = '$description' WHERE `username` = '$username' AND `macaddress` = '$macaddress';");
                $response["error"]=false;
            }
            else {
                $response["error"]=true;
            }
        }
        mysqli_set_charset($this->connection,"utf8");
        echo json_encode($response);
        mysqli_close($this->connection);
    }

    function deleteList($username, $key, $macaddress, $description){
        $sql_key = mysqli_query($this->connection, "SELECT `username`, `key`  FROM `appKey` WHERE `username` = '$username' AND `key` = '$key';");
        $response = array(); 
        while ($row_key = mysqli_fetch_array($sql_key, MYSQL_ASSOC)) {
            if(($row_key['key'] == $key) && ($row_key['username'] == $username)){
                $sql = mysqli_query($this->connection, "DELETE FROM `mac` WHERE `description` = '$description' AND `macaddress` = '$macaddress';");
                $response["error"]=false;
            }
            else{
                $response["error"]=true;
            }
        }
        echo json_encode($response);
        mysqli_close($this->connection);
    }

    function getWifi($username, $key, $wifidata, $textreport){
        $sql_key = mysqli_query($this->connection, "SELECT `key`, `username` FROM `appKey` WHERE `username` = '$username' AND `key` = '$key';");
        $row_key = mysqli_fetch_assoc($sql_key);
        $response = array(); 
        if($row_key['key'] == $key && $row_key['username'] == $username){
            $sql_wifi = mysqli_query($this->connection, "SELECT `username`, `wifi` FROM `wifi_data` WHERE `username` = '$username';");
            $row_wifi = mysqli_fetch_assoc($sql_wifi);
            if($row_wifi['username'] == NULL && $row_wifi['wifi'] == NULL){
                $sqll = mysqli_query($this->connection, "INSERT INTO `wifi_data` (`username`, `wifi`, `report`) VALUES ('$username', '$wifidata', '$textreport');");
                $response["error"]=false;
                $response["msg"]="pass1";
            }
            else if($row_wifi['username'] != NULL && $row_wifi['username'] == $username){
                $rows_in = mysqli_query($this->connection, "UPDATE `wifi_data` SET `wifi`='$wifidata', `report` = '$textreport' WHERE `username` = '$username';");
                $response["error"]=false;
                $response["msg"]="pass2";
            }
        }else{
            $response["error"]=true;
            $response["msg"]="dont pass";
        }
        echo json_encode($response);
        mysqli_close($this->connection);
    }

}

?>