const WebSocket = require('ws');
const fs = require('fs');

const socket = new WebSocket("ws://127.0.0.1:40295/devtools/browser/bac47339-b236-4979-a9f2-c97b4af5c6f9");

const payload1 = "{\"id\": 1,\"method\": \"Page.navigate\",\"params\": { \"url\": \"C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\temp\\current_page.html\"}}";
const payload3 = "{\"id\": 3,\"method\": \"Target.createTarget\",\"params\": { \"url\": \"http://microsoft.com\"}}";
const payload4 = "{\"id\": 4,\"method\": \"Target.attachToTarget\",\"params\": { \"targetId\": \"%s\", \"flatten\": true}}";
const payload5 = "{\"sessionId\": \"%s\", \"id\": 5,\"method\": \"Page.navigate\",\"params\": { \"url\": \"http://google.com\"}}";
const payload6 = "{\"sessionId\": \"%s\", \"id\": 6, \"method\": \"Page.printToPDF\", \"params\":{}}";

let targetId;
let sessionId;

// Fired when a connection with a WebSocket is opened
socket.onopen = function () {
	socket.send(payload3);
  /*setInterval(function() {
    if (socket.bufferedAmount == 0)
      socket.send(payload1);
  }, 50);*/
};

// Fired when data is received through a WebSocket
socket.onmessage = function(event) {
  console.log(event.data);
  response = JSON.parse(event.data);
  let id = response.id;
  console.log("> id: " + id);
  if (id != undefined) {
	  if (id == 3) {
		  targetId = response.result.targetId;
		  socket.send(payload4.replace("%s", targetId));
		  console.log("> payload4 sent targetId: " + targetId);
	  } else if (id == 4) {
		  sessionId = response.result.sessionId;
		  socket.send(payload5.replace("%s", sessionId));
		  console.log("> payload5 sent sessionId: " + sessionId);
	  } else if (id == 5) {
		  socket.send(payload6.replace("%s", sessionId));
		  console.log("> payload6 sent sessionId: " + sessionId); 
	  } else if (id == 6) {
		  fs.writeFile("test.pdf", Buffer.from(response.result.data, "base64")/*response.result.data*/,  function(err) {
			  if (err) {
				  return console.log(err);
			  }
			  console.log("file saved");
		  });
	  }
  }
};

// Fired when a connection with a WebSocket is closed
socket.onclose = function(event) {
  console.log(event);
};

// Fired when a connection with a WebSocket has been closed because of an error
socket.onerror = function(event) {
  console.log(event.data);
};