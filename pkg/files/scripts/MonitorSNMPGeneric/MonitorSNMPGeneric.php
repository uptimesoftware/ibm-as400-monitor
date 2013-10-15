<?php
	//error_reporting(E_ERROR | E_WARNING);
	$SNMP_Host = getenv('UPTIME_HOSTNAME');
	$SNMP_Port = getenv('UPTIME_SNMP-PORT');
	$SNMP_OID = getenv('UPTIME_SNMP-OID');
	$SNMP_WALK_INDEX_OID = getenv('UPTIME_SNMP-WALK-INDEX');
	$SNMP_WALK_INDEX_OID_INCLUDE = getenv('UPTIME_SNMP-WALK-INDEX-INCLUDE');
	$SNMP_WALK_INDEX_OID_INCLUDE = "/".$SNMP_WALK_INDEX_OID_INCLUDE."/";
	
	$SNMP_Data_Type = getenv('UPTIME_DATA_TYPE');
	//$SNMP_Data_Type = "string";
	
	$SNMP_Community = getenv('UPTIME_READ-COMMUNITY');
	
	
	$SNMP_action = getenv('UPTIME_SNMPACTION');
	$SNMP_version = getenv('UPTIME_SNMPVERSION');
	$SNMP_v3_agent = getenv('UPTIME_AGENT-USERNAME');
	$SNMP_v3_auth_type = getenv('UPTIME_AUTH-TYPE');
	$SNMP_v3_auth_pass = getenv('UPTIME_AUTH-PASS');
	$SNMP_v3_priv_type = getenv('UPTIME_PRIVACY-TYPE');
	$SNMP_v3_priv_pass = getenv('UPTIME_PRIVACY-PASS');
	$MONITOR_TIMEOUT = getenv('UPTIME_TIMEOUT');
	
	$CURRENT_TIME = time();
	
	$SNMP_Connection_String = $SNMP_Host . ":" . $SNMP_Port;
	
	$SNMP_Walk_Matched = false;
	
	if (!extension_loaded("snmp")) {
		echo "PHP SNMP Extension not loaded!";
		exit(2);
	}
	
	if($SNMP_OID == "") {
		echo "Please enter the OID";
		exit(2);
	} else {
		// PHP SNMP functions takes in OIDs like "1.3.6", not ".1.3.6".  Remove leading .
		if(substr($SNMP_OID,0,1) == ".") {
			$SNMP_OID=substr($SNMP_OID,1);
		}
	}
	if(($SNMP_action == "Walk")&&($SNMP_WALK_INDEX_OID == "")) {
		echo "Please enter the SNMP index OID";
		exit(2);
	} elseif(($SNMP_action == "Walk")&&($SNMP_WALK_INDEX_OID != "")) {
		if(substr($SNMP_WALK_INDEX_OID,0,1) == ".") {
			$SNMP_WALK_INDEX_OID=substr($SNMP_WALK_INDEX_OID,1);
		}
	}
	if(($SNMP_version == "v1")||($SNMP_version == "v2")) {
		if ($SNMP_Community == "") {
				echo "Please enter the SNMP community string.";
				exit(2);
		}
	}
	
	if($SNMP_version == "v1") {
		if($SNMP_action == "Get") {
			$returnedDataRaw = snmpget($SNMP_Connection_String,$SNMP_Community,$SNMP_OID);
			$returnedData = parseData($returnedDataRaw);
		} elseif($SNMP_action == "Walk") {
			$returnedDataRaw = snmpwalk($SNMP_Connection_String,$SNMP_Community,$SNMP_OID);

			$returnedData = parseData($returnedDataRaw);
			$returnedIndex = snmpwalk($SNMP_Connection_String,$SNMP_Community,$SNMP_WALK_INDEX_OID);
			$returnedIndex = parseData($returnedIndex);
		}
	} elseif($SNMP_version == "v2") {
		if($SNMP_action == "Get") {
			$returnedDataRaw = snmp2_get($SNMP_Connection_String,$SNMP_Community,$SNMP_OID);
			$returnedData = parseData($returnedDataRaw);
		} elseif($SNMP_action == "Walk") {
			$returnedDataRaw = snmp2_walk($SNMP_Connection_String,$SNMP_Community,$SNMP_OID);
			$returnedData = parseData($returnedDataRaw);
			$returnedIndex = snmp2_walk($SNMP_Connection_String,$SNMP_Community,$SNMP_WALK_INDEX_OID);
			$returnedIndex = parseData($returnedIndex);
		}

	}	elseif ($SNMP_version == "v3") {
	
		if ($SNMP_v3_agent == "") {
			echo "Please enter the SNMP v3 username";
			exit(2);
		}
	
		if ($SNMP_v3_priv_type == "") {
			if ($SNMP_v3_auth_type == "") {
				$SNMP_sec_level = "noAuthNoPriv";
			} else {
				$SNMP_sec_level = "authNoPriv";
				if ($SNMP_v3_auth_pass == "") {
					echo "Please enter the SNMP v3 authentication passphrase.";
					exit(2);
				}
			}
		} else {
			$SNMP_sec_level = "authPriv";
			if (($SNMP_v3_auth_pass == "") && ($SNMP_v3_priv_pass != "")) {
					echo "Please enter the SNMP v3 authentication passphrase.";
					exit(2);
			}
			if (($SNMP_v3_auth_pass != "") && ($SNMP_v3_priv_pass == "")) {
					echo "Please enter the SNMP v3 privacy passphrase.";
					exit(2);
			}
			if (($SNMP_v3_auth_pass == "") && ($SNMP_v3_priv_pass == "")) {
					echo "Please enter the SNMP v3 authentication & privacy passphrase.";
					exit(2);
			}
		}
		
		if($SNMP_action == "Get") {
			$returnedDataRaw = snmp3_get($SNMP_Connection_String,$SNMP_v3_agent,$SNMP_sec_level,$SNMP_v3_auth_type,$SNMP_v3_auth_pass,$SNMP_v3_priv_type,$SNMP_v3_priv_pass,$SNMP_OID);
			$returnedData = parseData($returnedDataRaw);
		} elseif($SNMP_action == "Walk") {
			$returnedDataRaw = snmp3_walk($SNMP_Connection_String,$SNMP_v3_agent,$SNMP_sec_level,$SNMP_v3_auth_type,$SNMP_v3_auth_pass,$SNMP_v3_priv_type,$SNMP_v3_priv_pass,$SNMP_OID);
			$returnedData = parseData($returnedDataRaw);
			$returnedIndex = snmp3_walk($SNMP_Connection_String,$SNMP_v3_agent,$SNMP_sec_level,$SNMP_v3_auth_type,$SNMP_v3_auth_pass,$SNMP_v3_priv_type,$SNMP_v3_priv_pass,$SNMP_WALK_INDEX_OID);			
			$returnedIndex = parseData($returnedIndex);
		}
		
		
	}

// Test if connection info is correct
if ($returnedData == false) {
	
	echo $SNMP_OID."Fail to get SNMP Data! Please check credentials\n";
	exit(2);
}

if($SNMP_action == "Get") {
	$dataType = getDataType($returnedDataRaw);
	//if($dataType == "integer") {
	if($SNMP_Data_Type == "Integer") {
		echo "returnedDataInt ".$returnedData."\n";
		//echo "returnedDataString notAString\n";
	} else {
		//echo "returnedDataInt 0\n";
		echo "returnedDataString ".$returnedData."\n";
	}

} elseif($SNMP_action == "Walk") {
	$dataType = getDataType($returnedDataRaw[0]);
	$returnedData_count = count($returnedData);
	for($i=0; $i < $returnedData_count; $i++) {
		//echo $returnedIndex[$i]."\n";
		$returnedIndex[$i] = str_replace(".","-",$returnedIndex[$i]);
		$returnedIndex[$i] = str_replace("\"","",$returnedIndex[$i]);
		$returnedIndex[$i] = str_replace(" ","_",$returnedIndex[$i]);
		$returnedIndex[$i] = str_replace(":","-",$returnedIndex[$i]);
				
		if(preg_match($SNMP_WALK_INDEX_OID_INCLUDE, $returnedIndex[$i])) {
			$SNMP_Walk_Matched = true;
			if($SNMP_Data_Type == "Integer") {
				echo $returnedIndex[$i].".returnedDataInt ". $returnedData[$i]."\n";
				//echo $returnedIndex[$i].".returnedDataString notAString\n";
				
			} else {
				//echo $returnedIndex[$i].".returnedDataInt 0\n";
				echo $returnedIndex[$i].".returnedDataString ".$returnedData[$i]."\n";
			}
			//echo "returnedDataRaw[$i]===".$returnedDataRaw[$i]."\n";
		}
		//echo "returnedData.".$returnedIndex[$i]." ". $returnedData[$i];
	}
	
	if ($SNMP_Walk_Matched == false) {
		echo "No matching index found.  Please check the OID index.\n";
		exit(2);
	}
}


function getLastValue($metricName,$LAST_VALUE_FILE) {
	
	//Initialize Variable
	$data[0] = $metricName;
	$data[1] = 0;
	$data[2] = 0;
	
	if (file_exists($LAST_VALUE_FILE)) {
		$handle = fopen($LAST_VALUE_FILE,"r+") or die("Can't open last value file for read");
		if ($handle) {
			while (!feof($handle)) // Loop til end of file.
			{
				$buffer = fgets($handle, 4096); // Read a line.
				if (preg_match("/".$metricName.".*/", $buffer)) // Check for string.
				{
					$data = preg_split("/-utNetapp-/", $buffer);
				}
			}
			fclose($handle); // Close the file.
		}
	}
	
	return $data;
}

function putLastValue($metricName, $value, $LAST_VALUE_FILE,$CURRENT_TIME) {
	
	// Look for the old value, remove it
	if (file_exists($LAST_VALUE_FILE)) {
		$contents = file_get_contents($LAST_VALUE_FILE);
		$contents = preg_replace("/".$metricName.".*/",'',$contents);
		$contents = preg_replace("/(^[\r\n]*|[\r\n]+)[\s\t]*[\r\n]+/", '', $contents);
		file_put_contents($LAST_VALUE_FILE,$contents);
	}

	$fh = fopen($LAST_VALUE_FILE,"a+") or die("Can't open last value file to write");
	if (flock($fh, LOCK_EX)) {
	
		$stringData = $metricName."-utNetapp-".$value."-utNetapp-".	$CURRENT_TIME."\n";
		
		//echo  $metricName."-utNetapp-".$value."-utNetapp-".	$CURRENT_TIME."<BR>";
		
		fwrite($fh,$stringData);
		fflush($fh);
		flock($fh, LOCK_UN);
	} else {
	
		echo "Can't lock file!\n";
	}
	fclose($fh);
}


function parseData($data) {
	if(is_array($data)) {
		$data_count=  count($data);
		for($i=0; $i < $data_count; $i++) {
			$data_output[$i] = strstr($data[$i], ':');
			$data_output[$i] = substr($data_output[$i], 1);
			$data_output[$i]=trim($data_output[$i]);
		}
	} else {
		$data_output = strstr($data, ':');
		$data_output = substr($data_output, 1);
		$data_output=trim($data_output);
	}
	return $data_output;
}

function getDataType($dataString) {
	$dataType = substr($dataString, 0, strrpos($dataString,':'));
	if (strpos($dataType,'STRING') !== false) {
		$returnDataType = "string";
	} else {
		$returnDataType = "integer";
	}
	return $returnDataType;
}

function get64($msb, $lsb) {
	$count = count($lsb);
	for($i=0; $i < $count; $i++) {
		$value[$i] = bcadd(bcmul($msb[$i], bcpow(2, 32)), $lsb[$i] >= 0?$lsb[$i]:bcsub(bcpow(2, 32), $lsb[$i])); // $a most significant bits, $b least significant bits
	}
	return $value;
}




?>