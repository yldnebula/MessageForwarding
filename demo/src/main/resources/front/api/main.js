function sendMsgSyncApi(data){
    return $axios({
        'url':'/index/sendSync',
        'method':'post',
        data
    })
}

function sendMsgAsyncApi(data){
    return $axios({
        'url':'/index/sendAsync',
        'method':'post',
        data
    })
}

