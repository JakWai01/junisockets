class WebSocketClient {
   
    constructor(protocol, hostname, port, endpoint) {

        this.webSocket = null;

        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
        this.endpoint = endpoint;
    }

    getServerUrl() { 
        return this.protocol + "://" + this.hostname + ":" + this.port + this.endpoint;
    }

    connect() {
       
        try {
            this.webSocket = new WebSocket(this.getServerUrl());

            //
            // Implement WebSocket event handlers!
            //
            this.webSocket.onopen = function(event) {
                console.log('onopen::' + JSON.stringify(event, null, 4));
            }

            this.webSocket.onmessage = function(event) {
                var msg = event.data;
                console.log('onmessage::' + JSON.stringify(msg, null, 4));
            }

            this.webSocket.onclose = function(event) { 
                console.log('onclose::' + JSON.stringify(event, null, 4));
            }

            this.webSocket.onerror = function(event) {
                console.log('onerror::' + JSON.stringify(event, null, 4));
            }
        } catch (exception) {
            console.error(exception);
        }
    }

    getStatus() {
        return this.webSocket.readyState;
    }

    send(message) {

        if (this.webSocket.readyState == WebSocket.OPEN) {
            this.webSocket.send(message);
        
        } else {
            console.error('webSocket is not open. readyState=' + this.webSocket.readyState);
        }
    }
}
