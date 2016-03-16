 <!DOCTYPE html>

	<html lang="en">
	<head>

	  	<meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
	  	<link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">
	 	<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
		<link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">
		<style type="text/css">
		    body {
		        background: url(bootstrap/img/glyphicons-halflings-white.png);
		    }
		</style>
	</head>
	<?php
		$DB_HOST = '';
		$DB_USER = '';
		$DB_PASS = '';
		$DB_NAME = '';
		$mysqli = mysqli_connect($DB_HOST, $DB_USER, $DB_PASS, $DB_NAME);
		$sql = mysqli_query($mysqli, "SELECT `username`, `report` FROM `wifi_data`;");
	?>
    <body>
    <div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                            <h3 id="myModalLabel">Delete</h3>
                        </div>
                        <div class="modal-body">
                            <p></p>
                        </div>
                        <div class="modal-footer">
                            <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                            <button data-dismiss="modal" class="btn red" id="btnYes">Confirm</button>
                        </div>
   </div>
    <div class="container">
	 	<h1>Report problem!</h1>

		<table class="table table-striped table-hover table-users">
			<thead>
		    <tr>
		        <th>#</th>
		        <th>Username</th>
		        <th>Problem</th>
		        <th></th>
		        <th></th>
		    </tr>
		    </thead>
		    <?php $i = 1; ?>
			<?php while($row = mysqli_fetch_assoc($sql)) { ?>
			<?php $name = $row['username']; ?>
			<?php $repo = $row['report']; ?>
			<tbody>
			  <tr class="info">
			  	<td><?php echo $i; ?></td>
			    <td><?php echo $name; ?></td>
			    <td><?php echo $repo; ?></td>
			    <td> <?php echo "<button type=\"button\" class=\"btn btn-warning btn-md\"><a href=drawGraph.php?name=" . $name."> Graph </button></a>" ?></td> 
			    <td> <?php echo "<button type=\"button\" class=\"btn btn-warning btn-md\"><a href=delete.php?name=" . $name."> Delete </button></a>" ?></td> 
			  </tr>
			</tbody>
			  <?php $name = $row['username']; ?>
			  <?php $i++; ?>
			  <?php } ?>
		</table>

		<script src="bootstrap/js/jquery.js"></script>
		<script src="bootstrap/js/bootstrap.min.js"></script>
		<script>
			 $('#myModal').on('show', function() {
    			var tit = $('.confirm-delete').data('title');

    			$('#myModal .modal-body p').html("Are you sure? " + '<b>' + tit +'</b>' + ' ?');
    			var id = $(this).data('id'), removeBtn = $(this).find('.danger');
			})

			$('.confirm-delete').on('click', function(e) {
			    e.preventDefault();

			    var id = $(this).data('id');
			    $('#myModal').data('id', id).modal('show');
			});

			$('#btnYes').click(function() {
			    // handle deletion here
			    var id = $('#myModal').data('id');
			    $('[data-id='+id+']').parents('tr').remove();
			    $('#myModal').modal('hide');
			    
			});
		</script>
		</div>
    </body>
</html>



