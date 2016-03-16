<?php
    /**
     * Step 1: Require the Slim Framework
     *
     * If you are not using Composer, you need to require the
     * Slim Framework and register its PSR-0 autoloader.
     *
     * If you are using Composer, you can skip this step.
     */
    require 'libs/Slim/Slim.php';

    \Slim\Slim::registerAutoloader();

    require_once("Rest.inc.php");
    require_once("include/DbFunction.php");
    
    /**
     * Step 2: Instantiate a Slim application
     *
     * This example instantiates a Slim application using
     * its default settings. However, you will usually configure
     * your Slim application now by passing an associative array
     * of setting names and values into the application constructor.    
     */
    $app = new \Slim\Slim();

    /**
     * Step 3: Define the Slim application routes
     *
     * Here we define several Slim application routes that respond
     * to appropriate HTTP request methods. In this example, the second
     * argument for `Slim::get`, `Slim::post`, `Slim::put`, `Slim::patch`, and `Slim::delete`
     * is an anonymous function.
     */
        
    /*
    *  Database connection 
    */

    ///////////////////////////////////////////////////////////////////////////////////////
    ////                             Authentication                                    ////
    ///////////////////////////////////////////////////////////////////////////////////////
    $app->post('/userAuth', function () use($app) {
        $username = $app->request()->post('username');
        $password = $app->request()->post('password');
        $df = new DbFunction();
        $response = array();
        $ch = $df->checkLogin($username, $password);
        if ($ch != false) {
            $response["error"] = FALSE;
            $response["key"] = $ch["key"];
            echo json_encode($response);
        } else {
            $response["error"] = TRUE;
            $response["error_msg"] = "Please try again!";
            echo json_encode($response);
        }
    });

    ///////////////////////////////////////////////////////////////////////////////////////
    ////                           Create List Device                                  ////
    ///////////////////////////////////////////////////////////////////////////////////////
    $app->post('/listDevice', function () use($app){
        $username = $app->request()->post('username');
        $key = $app->request()->post('key');
        $df = new DbFunction();
        $df->createList($username, $key);
    });

    ///////////////////////////////////////////////////////////////////////////////////////
    ////                                Edit Device                                    ////
    ///////////////////////////////////////////////////////////////////////////////////////
    $app->post('/editDevice', function () use($app){
        $username = $app->request()->post('username');
        $key = $app->request()->post('key');
        $macaddress = $app->request()->post('macaddress');
        $description = $app->request()->post('description');
        $df = new DbFunction();
        $df->editName($username, $key, $macaddress, $description);
    });

  

    ///////////////////////////////////////////////////////////////////////////////////////
    ////                               Delete Device                                   ////
    ///////////////////////////////////////////////////////////////////////////////////////
    $app->post('/deleteDevice/', function () use($app){
        $username = $app->request()->post('username');
        $key = $app->request()->post('key');
        $macaddress = $app->request()->post('macaddress');
        $description = $app->request()->post('description');
        $df = new DbFunction();
        $df->deleteList($username, $key, $macaddress, $description);
    });

    ///////////////////////////////////////////////////////////////////////////////////////
    ////                                   Get Wifi                                    ////
    ///////////////////////////////////////////////////////////////////////////////////////
    $app->post('/reportDevice/', function () use($app){
        $username = $app->request()->post('username');
        $key = $app->request()->post('key');
        $wifidata = $app->request()->post('wifidata');
        $textreport = $app->request()->post('textreport');

        $df = new DbFunction();
        $df->getWifi($username, $key, $wifidata, $textreport);
    });


    //...................Run...................//
    $app->run();
?>