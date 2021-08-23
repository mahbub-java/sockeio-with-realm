
//please sir node this index.js in localhost or any other host where node server running...

var express    = require('express');
var app        = express();  
var http = require('http').Server(app);
var io = require('socket.io')(http);
var port = process.env.PORT || 3000;


io.on('connection', function (socket) {

  console.log('one user connected : '+socket.id);
  
  //when new user subscribe, its emits uniqueRoomName of user
  
  socket.on('subscribe', (data) => {
	  
		console.log('new subscribe');
	
		const userData = JSON.parse(data);
	
		var id = userData.id;
		var name = userData.name;
		var roomName = userData.roomName;
		
		socket.join(`${roomName}`);
		
		io.to(`${roomName}`).emit('newUserInValidRoom', roomName);
		
	});
	
	// after validate from app side, app emits new message and this brodcast message his Unique Room
	// as I am trying to do 1-1 chat 
	
	socket.on('newMessage', (data) => {
	  
		console.log('new message');
	
		const userData = JSON.parse(data);
	
		var roomName = userData.roomName;
		
		console.log('new message on room:'+roomName);
				
		socket.broadcast.to(`${roomName}`).emit('updateChat',JSON.stringify(userData));
		
	});

});


http.listen(port, function () {
	console.log("Welcome to socket, task give by kotha");
	console.log('Server listening at port %d', port);
});