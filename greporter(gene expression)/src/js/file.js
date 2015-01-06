var spawn = require('child_process').spawn,

function    (dispath,bin)
{
    which    = spawn('which', bin);
	which.stdout.on('data', function (data) {
  			$('#'+dispath+"path").val(data);
		});

}

