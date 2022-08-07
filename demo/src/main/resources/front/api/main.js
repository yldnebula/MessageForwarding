function sendMsgSyncApi(data) {
    return $axios({
        'url': '/index/sendSync',
        'method': 'post',
        data
    })
}

function sendMsgAsyncApi(data) {
    return $axios({
        'url': '/index/sendAsync',
        'method': 'post',
        data
    })
}

function getOnlineUser() {
    return $axios({
        'url': '/index/users',
        'method': 'get',
    })
}

function connect(username, success, fail, onOpen, onMessage, onClose) {
    var that = this;
    let ws = null;
    if (username != null && username !== "") {
        if ('WebSocket' in window) {
            ws = new WebSocket("ws://"+baseUrl+"/socketServer/" + username);
            success(ws);
        } else if ('MozWebSocket' in window) {
            ws = new MozWebSocket("ws://"+baseUrl+"/socketServer/" + username);
            success(ws);
        } else {
            alert("该浏览器不支持websocket");
            fail();
            return;
        }

        ws.onmessage = function (evt){
            onMessage(evt);
        };

        ws.onclose = onClose

        ws.onopen = onOpen

    } else {
        alert("请输入您的Id");
    }
}

