<?php
	require_once('libs/jpgraph-1.27.1/src/jpgraph.php');
	require_once('libs/jpgraph-1.27.1/src/jpgraph_line.php'); 
	
	$DB_HOST = '';
	$DB_USER = '';
	$DB_PASS = '';
	$DB_NAME = '';
	$name = $_GET['name'];
	$mysqli = mysqli_connect($DB_HOST, $DB_USER, $DB_PASS, $DB_NAME);
	$sql = mysqli_query($mysqli, "SELECT `username`, `wifi` FROM `wifi_data` WHERE username = '$name';");
	$row_sql = mysqli_fetch_assoc($sql);

	$text = $row_sql['wifi'];
    $data = json_decode($text);
    // echo "----------->" .sizeof($data->data). "<-----------" . "</br>";
    //...........................................Setting the graph...........................................//

    // $color = array("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#CCEEFF", "#071BFC", "#FC07E8", "#07F8FC", "#07FC40", "#E4FC07", "#FC3C07", "#E34788", "#E347D6",
				//    "#6647E3", "#47E3E3", "#47E375", "#CEE347", "#E37B47", "#F4CCB8", "#6B5C55", "#171413");

	// Create the graph. These two calls are always required
	$graph = new Graph(1550,800);
	$graph->SetScale('linint');

	// $theme_class=new UniversalTheme;

	// $graph->SetTheme($theme_class);
	// $graph->img->SetAntiAliasing(false);
	$graph->title->Set('Report Problem');
	$graph->SetBox(false);

	// $graph->img->SetAntiAliasing();
	$graph->yaxis->SetTitle('RSSI [dBm]','middle');  
	$graph->yaxis->HideZeroLabel();
	// $graph->yaxis->HideLine(false);
	// $graph->yaxis->HideTicks(false,false);
	$graph->yaxis->scale->SetAutoMin(-100);
	$graph->yaxis->scale->SetAutoMax(-30);

	$graph->xaxis->SetTitle('Time [second]' ); 
	// $graph->xgrid->Show();
	$graph->xgrid->SetLineStyle("solid");
	$graph->xaxis->HideFirstLastLabel(); 
	$graph->xgrid->SetColor('#E3E3E3');
	// $graph->xaxis->SetLabelSide(SIDE_UP);
	$graph->xaxis->Hide(); // Hide xaxis 

    //..........................................Fetch Data............................................//
	
	$data_rssi = array();
	$data_ssid = array();
	$data_bssid = array();
	for ($i=0; $i < sizeof($data->data); $i++) { 
		for ($j=0; $j < sizeof($data->data[$i]->count); $j++) {
			$data_bssid[] = $data->data[$i]->count[$j]->bssid;
	    	$data_ssid[] = $data->data[$i]->count[$j]->ssid;
	    	$data_rssi[] = $data->data[$i]->count[$j]->rssi;
		}
	}


	$terms = array_fill_keys($data_bssid, array());

    foreach ($data_bssid as $i => $term) {
    	$terms[$term][] = $data_rssi[$i];
    }
	// $terms2 = array_fill_keys($data_ssid, array());
 //    foreach ($data_ssid as $key => $value) {
 //    	$terms2[$value][] = $data_bssid[$i];
 //    }
    // echo sizeof($terms2);
    // print_r($terms2);
    // for
    // $fruit = array_shift($terms2);

    // print_r($fruit);
    // echo "-------------------------------------------------";

	foreach ($terms as $ap_name => $value) {
		for ($i=0; $i < count($terms[$ap_name]); $i++) { 
			// $terms[$ap_name] = $data_ssid[$i];	//Add last
			$datay[] = $terms[$ap_name][$i];
		}	

		// Create the first line
		$p[$ap_name] = new LinePlot($datay);
		
		$graph->Add($p[$ap_name]);
		$color = rand_color();
		$p[$ap_name]->SetColor($color);
		$p[$ap_name]->SetLegend($ap_name);
		$datay = array("");
	}

	$graph->legend->SetFrameWeight(1);

	// Output line
	$graph->Stroke();

?>

<?php
	function rand_color(){
		$color = '#'. dechex(rand(0x000000, 0xFFFFFF));
		return $color;
	}

	function objectToArray($d) {
        if (is_object($d)) {
            // Gets the properties of the given object
            // with get_object_vars function
            $d = get_object_vars($d);
        }

        if (is_array($d)) {
            /*
            * Return array converted to object
            * Using __FUNCTION__ (Magic constant)
            * for recursive call
            */
            return array_map(__FUNCTION__, $d);
        }
        else {
            // Return array
            return $d;
        }
    }
?>