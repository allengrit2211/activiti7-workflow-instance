/**
 * 娉ㄥ唽鍛藉悕绌洪棿
 * @param {String} fullNS 瀹屾暣鐨勫懡鍚嶇┖闂村瓧绗︿覆锛屽qui.dialog
 * @param {Boolean} isIgnorSelf 鏄惁蹇界暐鑷繁锛岄粯璁や负false锛屼笉蹇界暐
 * @example
 *      window.registNS("QingFeed.Text.Bold");
 */
window.registNS = function(fullNS,isIgnorSelf){
    //鍛藉悕绌洪棿鍚堟硶鎬ф牎楠屼緷鎹�
    var reg = /^[_$a-z]+[_$a-z0-9]*/i;

    // 灏嗗懡鍚嶇┖闂村垏鎴怤閮ㄥ垎, 姣斿baidu.libs.Firefox绛�
    var nsArray = fullNS.split('.');
    var sEval = "";
    var sNS = "";
    var n = isIgnorSelf ? nsArray.length - 1 : nsArray.length;
    for (var i = 0; i < n; i++){
        //鍛藉悕绌洪棿鍚堟硶鎬ф牎楠�
        if(!reg.test(nsArray[i])) {
            throw new Error("Invalid namespace:" + nsArray[i] + "");
            return ;
        }
        if (i != 0) sNS += ".";
        sNS += nsArray[i];
        // 渚濇鍒涘缓鏋勯€犲懡鍚嶇┖闂村璞★紙鍋囧涓嶅瓨鍦ㄧ殑璇濓級鐨勮鍙�
        sEval += "if(typeof(" + sNS + ")=='undefined') " + sNS + "=new Object();else " + sNS + ";";
    }
    //鐢熸垚鍛藉悕绌洪棿
    if (sEval != "") {
        return eval(sEval);
    }
    return {};
};


/**
 * 娉ㄥ唽鍛藉悕绌洪棿
 */
window.registNS('bpmn');

/**
 * @class bpmn.LocalStorage
 * 璺ㄦ祻瑙堝櫒鐨勬湰鍦板瓨鍌ㄥ疄鐜般€傞珮绾ф祻瑙堝櫒浣跨敤localstorage锛宨e浣跨敤UserData銆傝櫧鐒惰鏄湰鍦板瓨鍌紝涔熻涓嶈瀛樺偍杩囧ぇ鏁版嵁锛屾渶濂戒笉瑕佸ぇ浜�64K.
 * 鍥犱负ie涓婾serData姣忛〉鏈€澶у瓨鍌ㄦ槸64k銆�
 * @singleton
 */
(function(){
    /**
     * 楠岃瘉瀛楃涓叉槸鍚﹀悎娉曠殑閿悕
     * @param {Object} key 寰呴獙璇佺殑key
     * @return {Boolean} true锛氬悎娉曪紝false锛氫笉鍚堟硶
     * @private
     */
    function _isValidKey(key) {
        return (new RegExp("^[^\\x00-\\x20\\x7f\\(\\)<>@,;:\\\\\\\"\\[\\]\\?=\\{\\}\\/\\u0080-\\uffff]+\x24")).test(key);
    }
    //鎵€鏈夌殑key
    var _clearAllKey = "_bpmn.ALL.KEY_";

    /**
     * 鍒涘缓骞惰幏鍙栬繖涓猧nput:hidden瀹炰緥
     * @return {HTMLInputElement} input:hidden瀹炰緥
     * @private
     */
    function _getInstance(){
        //鎶奤serData缁戝畾鍒癷nput:hidden涓�
        var _input = null;
        //鏄殑锛屼笉瑕佹儕璁讹紝杩欓噷姣忔閮戒細鍒涘缓涓€涓猧nput:hidden骞跺鍔犲埌DOM鏍戠
        //鐩殑鏄伩鍏嶆暟鎹閲嶅鍐欏叆锛屾彁鏃╅€犳垚鈥滅鐩樼┖闂村啓婊♀€濈殑Exception
        _input = document.createElement("input");
        _input.type = "hidden";
        _input.addBehavior("#default#userData");
        document.body.appendChild(_input);
        return _input;
    }

    /**
     * 灏嗘暟鎹€氳繃UserData鐨勬柟寮忎繚瀛樺埌鏈湴锛屾枃浠跺悕涓猴細鏂囦欢鍚嶄负锛歝onfig.key[1].xml
     * @param {String} key 寰呭瓨鍌ㄦ暟鎹殑key锛屽拰config鍙傛暟涓殑key鏄竴鏍风殑
     * @param {Object} config 寰呭瓨鍌ㄦ暟鎹浉鍏抽厤缃�
     * @cofnig {String} key 寰呭瓨鍌ㄦ暟鎹殑key
     * @config {String} value 寰呭瓨鍌ㄦ暟鎹殑鍐呭
     * @config {String|Object} [expires] 鏁版嵁鐨勮繃鏈熸椂闂达紝鍙互鏄暟瀛楋紝鍗曚綅鏄绉掞紱涔熷彲浠ユ槸鏃ユ湡瀵硅薄锛岃〃绀鸿繃鏈熸椂闂�
     * @private
     */
    function __setItem(key,config){
        try {
            var input = _getInstance();
            //鍒涘缓涓€涓猄torage瀵硅薄
            var storageInfo = config || {};
            //璁剧疆杩囨湡鏃堕棿
            if(storageInfo.expires) {
                var expires;
                //濡傛灉璁剧疆椤归噷鐨別xpires涓烘暟瀛楋紝鍒欒〃绀烘暟鎹殑鑳藉瓨娲荤殑姣鏁�
                if ('number' == typeof storageInfo.expires) {
                    expires = new Date();
                    expires.setTime(expires.getTime() + storageInfo.expires);
                }
                input.expires = expires.toUTCString();
            }

            //瀛樺偍鏁版嵁
            input.setAttribute(storageInfo.key,storageInfo.value);
            //瀛樺偍鍒版湰鍦版枃浠讹紝鏂囦欢鍚嶄负锛歴torageInfo.key[1].xml
            input.save(storageInfo.key);
        } catch (e) {
        }
    }

    /**
     * 灏嗘暟鎹€氳繃UserData鐨勬柟寮忎繚瀛樺埌鏈湴锛屾枃浠跺悕涓猴細鏂囦欢鍚嶄负锛歝onfig.key[1].xml
     * @param {String} key 寰呭瓨鍌ㄦ暟鎹殑key锛屽拰config鍙傛暟涓殑key鏄竴鏍风殑
     * @param {Object} config 寰呭瓨鍌ㄦ暟鎹浉鍏抽厤缃�
     * @cofnig {String} key 寰呭瓨鍌ㄦ暟鎹殑key
     * @config {String} value 寰呭瓨鍌ㄦ暟鎹殑鍐呭
     * @config {String|Object} [expires] 鏁版嵁鐨勮繃鏈熸椂闂达紝鍙互鏄暟瀛楋紝鍗曚綅鏄绉掞紱涔熷彲浠ユ槸鏃ユ湡瀵硅薄锛岃〃绀鸿繃鏈熸椂闂�
     * @private
     */
    function _setItem(key,config){
        //淇濆瓨鏈夋晥鍐呭
        __setItem(key,config);

        //涓嬮潰鐨勪唬鐮佺敤鏉ヨ褰曞綋鍓嶄繚瀛樼殑key锛屼究浜庝互鍚巆learAll
        var result = _getItem({key : _clearAllKey});
        if(result) {
            result = {
                key : _clearAllKey,
                value : result
            };
        } else {
            result = {
                key : _clearAllKey,
                value : ""
            };
        }

        if(!(new RegExp("(^|\\|)" + key + "(\\||$)",'g')).test(result.value)) {
            result.value += "|" + key;
            //淇濆瓨閿�
            __setItem(_clearAllKey,result);
        }
    }

    /**
     * 鎻愬彇鏈湴瀛樺偍鐨勬暟鎹�
     * @param {String} config 寰呰幏鍙栫殑瀛樺偍鏁版嵁鐩稿叧閰嶇疆
     * @cofnig {String} key 寰呰幏鍙栫殑鏁版嵁鐨刱ey
     * @return {String} 鏈湴瀛樺偍鐨勬暟鎹紝鑾峰彇涓嶅埌鏃惰繑鍥瀗ull
     * @example
     * bpmn.LocalStorage.get({
     *      key : "username"
     * });
     * @private
     */
    function _getItem(config){
        try {
            var input = _getInstance();
            //杞藉叆鏈湴鏂囦欢锛屾枃浠跺悕涓猴細config.key[1].xml
            input.load(config.key);
            //鍙栧緱鏁版嵁
            return input.getAttribute(config.key) || null;
        } catch (e) {
            return null;
        }
    }

    /**
     * 绉婚櫎鏌愰」瀛樺偍鏁版嵁
     * @param {Object} config 閰嶇疆鍙傛暟
     * @cofnig {String} key 寰呭瓨鍌ㄦ暟鎹殑key
     * @private
     */
    function _removeItem(config){
        try {
            var input = _getInstance();
            //杞藉叆瀛樺偍鍖哄潡
            input.load(config.key);
            //绉婚櫎閰嶇疆椤�
            input.removeAttribute(config.key);
            //寮哄埗浣垮叾杩囨湡
            var expires = new Date();
            expires.setTime(expires.getTime() - 1);
            input.expires = expires.toUTCString();
            input.save(config.key);

            //浠巃llkey涓垹闄ゅ綋鍓峩ey
            //涓嬮潰鐨勪唬鐮佺敤鏉ヨ褰曞綋鍓嶄繚瀛樼殑key锛屼究浜庝互鍚巆learAll
            var result = _getItem({key : _clearAllKey});
            if(result) {
                result = result.replace(new RegExp("(^|\\|)" + config.key + "(\\||$)",'g'),'');
                result = {
                    key : _clearAllKey,
                    value : result
                };
                //淇濆瓨閿�
                __setItem(_clearAllKey,result);
            }

        } catch (e) {
        }
    }

    //绉婚櫎鎵€鏈夌殑鏈湴鏁版嵁
    function _clearAll(){
        result = _getItem({key : _clearAllKey});
        if(result) {
            var allKeys = result.split("|");
            var count = allKeys.length;
            for(var i = 0;i < count;i++){
                if(!!allKeys[i]) {
                    _removeItem({key:allKeys[i]});
                }
            }
        }
    }


    /**
     * 鑾峰彇鎵€鏈夌殑鏈湴瀛樺偍鏁版嵁瀵瑰簲鐨刱ey
     * @return {Array} 鎵€鏈夌殑key
     * @private
     */
    function _getAllKeys(){
        var result = [];
        var keys = _getItem({key : _clearAllKey});
        if(keys) {
            keys = keys.split('|');
            for(var i = 0,len = keys.length;i < len;i++){
                if(!!keys[i]) {
                    result.push(keys[i]);
                }
            }
        }
        return result ;
    }

    /**
     * 鍒ゆ柇褰撳墠娴忚鍣ㄦ槸鍚︽敮鎸佹湰鍦板瓨鍌細window.localStorage
     * @return {Boolean} true锛氭敮鎸侊紱false锛氫笉鏀寔(jQuery.browser寤鸿寮冪敤锛屽彲浠ヤ娇鐢╦Query.support鏉ヤ唬鏇�)
     * @remark 鏀寔鏈湴瀛樺偍鐨勬祻瑙堝櫒锛欼E8+銆丗irefox3.0+銆丱pera10.5+銆丆hrome4.0+銆丼afari4.0+銆乮Phone2.0+銆丄ndrioid2.0+
     * @private
     */
    var _isSupportLocalStorage = (('localStorage' in window) && (window['localStorage'] !== null)),
        _isSupportUserData = !!jQuery.support.ie;
    bpmn.LocalStorage = {
        /**
         * 濡傛灉鏀寔鏈湴瀛樺偍锛岃繑鍥瀟rue锛涘惁鍒欒繑鍥瀎alse
         * @type Boolean
         */
        isAvailable : _isSupportLocalStorage || _isSupportUserData,

        /**
         * 灏嗘暟鎹繘琛屾湰鍦板瓨鍌紙鍙兘瀛樺偍瀛楃涓蹭俊鎭級
         * <pre><code>
         * //淇濆瓨鍗曚釜瀵硅薄
         * bpmn.LocalStorage.set({
         * 		key : "username",
         * 		value : "baiduie",
         * 		expires : 3600 * 1000
         * });
         * //淇濆瓨瀵逛釜瀵硅薄
         * bpmn.LocalStorage.set([{
         * 		key : "username",
         * 		value : "baiduie",
         * 		expires : 3600 * 1000
         * },{
         * 		key : "password",
         * 		value : "zxlie",
         * 		expires : 3600 * 1000
         * }]);
         * </code></pre>
         * @param {Object} obj 寰呭瓨鍌ㄦ暟鎹浉鍏抽厤缃紝鍙互鏄崟涓狫SON瀵硅薄锛屼篃鍙互鏄敱澶氫釜JSON瀵硅薄缁勬垚鐨勬暟缁�
         * <ul>
         * <li><b>key</b> : String <div class="sub-desc">寰呭瓨鍌ㄦ暟鎹殑key锛屽姟蹇呭皢key鍊艰捣鐨勫鏉備竴浜涳紝濡傦細bpmn.username</div></li>
         * <li><b>value</b> : String <div class="sub-desc">寰呭瓨鍌ㄦ暟鎹殑鍐呭</div></li>
         * <li><b>expires</b> : String/Object (Optional)<div class="sub-desc">鏁版嵁鐨勮繃鏈熸椂闂达紝鍙互鏄暟瀛楋紝鍗曚綅鏄绉掞紱涔熷彲浠ユ槸鏃ユ湡瀵硅薄锛岃〃绀鸿繃鏈熸椂闂�</div></li>
         * </ul>
         */
        set : function(obj){
            //淇濆瓨鍗曚釜瀵硅薄
            var _set_ = function(config){
                //key鏍￠獙
                if(!_isValidKey(config.key)) {return;}

                //寰呭瓨鍌ㄧ殑鏁版嵁
                var storageInfo = config || {};

                //鏀寔鏈湴瀛樺偍鐨勬祻瑙堝櫒锛欼E8+銆丗irefox3.0+銆丱pera10.5+銆丆hrome4.0+銆丼afari4.0+銆乮Phone2.0+銆丄ndrioid2.0+
                if(_isSupportLocalStorage) {
                    window.localStorage.setItem(storageInfo.key,storageInfo.value);
                    if(config.expires) {
                        var expires;
                        //濡傛灉璁剧疆椤归噷鐨別xpires涓烘暟瀛楋紝鍒欒〃绀烘暟鎹殑鑳藉瓨娲荤殑姣鏁�
                        if ('number' == typeof storageInfo.expires) {
                            expires = new Date();
                            expires.setTime(expires.getTime() + storageInfo.expires);
                        }

                        window.localStorage.setItem(storageInfo.key + ".expires",expires);
                    }
                } else if(_isSupportUserData) { //IE7鍙婁互涓嬬増鏈紝閲囩敤UserData鏂瑰紡
                    _setItem(config.key,storageInfo);
                }
            };

            //鍒ゆ柇浼犲叆鐨勫弬鏁版槸鍚︿负鏁扮粍
            if(obj && obj.constructor === Array && obj instanceof Array){
                for(var i = 0,len = obj.length;i < len;i++){
                    _set_(obj[i]);
                }
            }else if(obj){
                _set_(obj);
            }
        },

        /**
         * 鎻愬彇鏈湴瀛樺偍鐨勬暟鎹�
         * <pre><code>
         * //鑾峰彇鏌愪竴涓湰鍦板瓨鍌紝杩斿洖鍊间负锛歿key:"",value:"",expires:""}锛屾湭鍙栧埌鍊兼椂杩斿洖鍊间负锛歯ull
         * var rst = bpmn.LocalStorage.get({
         * 		key : "username"
         * });
         * //鑾峰彇澶氫釜鏈湴瀛樺偍锛岃繑鍥炲€间负锛歔"","",""]锛屾湭鍙栧埌鍊兼椂杩斿洖鍊间负锛歔null,null,null]
         * bpmn.LocalStorage.get([{
         * 		key : "username"
         * },{
         * 		key : "password"
         * },{
         * 		key : "sex"
         * }]);
         * </code></pre>
         * @param {String} obj 寰呰幏鍙栫殑瀛樺偍鏁版嵁鐩稿叧閰嶇疆锛屾敮鎸佸崟涓璞′紶鍏ワ紝鍚屾牱涔熸敮鎸佸涓璞″皝瑁呯殑鏁扮粍鏍煎紡
         * @config {String} key 寰呭瓨鍌ㄦ暟鎹殑key
         * @return {String} 鏈湴瀛樺偍鐨勬暟鎹紝浼犲叆涓哄崟涓璞℃椂锛岃繑鍥炲崟涓璞★紝鑾峰彇涓嶅埌鏃惰繑鍥瀗ull锛涗紶鍏ヤ负鏁扮粍鏃讹紝杩斿洖涓烘暟缁�
         */
        get : function(obj){
            //鑾峰彇鏌愪竴涓湰鍦板瓨鍌�
            var _get_ = function(config){
                //缁撴灉
                var result = null;
                if(typeof config === "string") config = {key : config};
                //key鏍￠獙
                if(!_isValidKey(config.key)) {return result;}

                //鏀寔鏈湴瀛樺偍鐨勬祻瑙堝櫒锛欼E8+銆丗irefox3.0+銆丱pera10.5+銆丆hrome4.0+銆丼afari4.0+銆乮Phone2.0+銆丄ndrioid2.0+
                if(_isSupportLocalStorage) {
                    result = window.localStorage.getItem(config.key);
                    //杩囨湡鏃堕棿鍒ゆ柇锛屽鏋滆繃鏈熶簡锛屽垯绉婚櫎璇ラ」
                    if(result) {
                        var expires = window.localStorage.getItem(config.key + ".expires");
                        result = {
                            value : result,
                            expires : expires ? new Date(expires) : null
                        };
                        if(result && result.expires && result.expires < new Date()) {
                            result = null;
                            window.localStorage.removeItem(config.key);
                            window.localStorage.removeItem(config.key + ".expires");
                        }
                    }
                } else if(_isSupportUserData) { //IE7鍙婁互涓嬬増鏈紝閲囩敤UserData鏂瑰紡
                    //杩欓噷涓嶇敤鍗曠嫭鍒ゆ柇鍏秂xpires锛屽洜涓篣serData鏈韩鍏锋湁杩欎釜鍒ゆ柇
                    result = _getItem(config);
                    if(result) {
                        result = { value : result };
                    }
                }

                return result ? result.value : "";
            };

            var rst = null;
            //鍒ゆ柇浼犲叆鐨勫弬鏁版槸鍚︿负鏁扮粍
            if(obj && obj.constructor === Array && obj instanceof Array){
                rst = [];
                for(var i = 0,len = obj.length;i < len;i++){
                    rst.push(_get_(obj[i]));
                }
            }else if(obj){
                rst = _get_(obj);
            }
            return rst;
        },

        /**
         * 绉婚櫎鏌愪竴椤规湰鍦板瓨鍌ㄧ殑鏁版嵁
         * <pre><code>
         * //鍒犻櫎涓€涓湰鍦板瓨鍌ㄩ」
         * bpmn.LocalStorage.remove({
         * 		key : "username"
         * });
         * //鍒犻櫎澶氫釜鏈湴瀛樺偍椤圭洰 *
         * bpmn.LocalStorage.remove([{
         * 		key : "username"
         * },{
         * 		key : "password"
         * },{
         * 		key : "sex"
         * }]);
         * </code></pre>
         * @param {String} obj 寰呯Щ闄ょ殑瀛樺偍鏁版嵁鐩稿叧閰嶇疆锛屾敮鎸佺Щ闄ゆ煇涓€涓湰鍦板瓨鍌紝涔熸敮鎸佹暟缁勫舰寮忕殑鎵归噺绉婚櫎
         * @config {String} key 寰呯Щ闄ゆ暟鎹殑key
         * @return 鏃�
         */
        remove : function(obj){
            //绉婚櫎鏌愪竴椤规湰鍦板瓨鍌ㄧ殑鏁版嵁
            var _remove_ = function(config){
                //鏀寔鏈湴瀛樺偍鐨勬祻瑙堝櫒锛欼E8+銆丗irefox3.0+銆丱pera10.5+銆丆hrome4.0+銆丼afari4.0+銆乮Phone2.0+銆丄ndrioid2.0+
                if(_isSupportLocalStorage) {
                    window.localStorage.removeItem(config.key);
                    window.localStorage.removeItem(config.key + ".expires");
                } else if(_isSupportUserData){ //IE7鍙婁互涓嬬増鏈紝閲囩敤UserData鏂瑰紡
                    _removeItem(config);
                }
            };

            //鍒ゆ柇浼犲叆鐨勫弬鏁版槸鍚︿负鏁扮粍
            if(obj && obj.constructor === Array && obj instanceof Array){
                for(var i = 0,len = obj.length;i < len;i++){
                    _remove_(obj[i]);
                }
            }else if(obj){
                _remove_(obj);
            }
        },

        /**
         * 娓呴櫎鎵€鏈夋湰鍦板瓨鍌ㄧ殑鏁版嵁
         * <pre><code>
         * bpmn.LocalStorage.clearAll();
         * </code></pre>
         */
        clearAll : function(){
            //鏀寔鏈湴瀛樺偍鐨勬祻瑙堝櫒锛欼E8+銆丗irefox3.0+銆丱pera10.5+銆丆hrome4.0+銆丼afari4.0+銆乮Phone2.0+銆丄ndrioid2.0+
            if(_isSupportLocalStorage) {
                window.localStorage.clear();
            } else if(_isSupportUserData) { //IE7鍙婁互涓嬬増鏈紝閲囩敤UserData鏂瑰紡
                _clearAll();
            }
        },

        //淇濆瓨鍗曚釜瀵硅薄鍒版湰鍦�
        save:function(key,value){
            bpmn.LocalStorage.set({
                key : key,
                value : value,
                expires : 30 * 12 * 3600 * 1000  /*鍗曚綅锛歮s 杩欓噷缂撳瓨涓€涓湀*/
            });
        },
        /**
         * 鑾峰彇鎵€鏈夌殑鏈湴瀛樺偍鏁版嵁瀵瑰簲鐨刱ey
         * <pre><code>
         * var keys = bpmn.LocalStorage.getAllKeys();
         * </code></pre>
         * @return {Array} 鎵€鏈夌殑key
         */
        getAllKeys : function(){
            var result = [];
            //鏀寔鏈湴瀛樺偍鐨勬祻瑙堝櫒锛欼E8+銆丗irefox3.0+銆丱pera10.5+銆丆hrome4.0+銆丼afari4.0+銆乮Phone2.0+銆丄ndrioid2.0+
            if(_isSupportLocalStorage) {
                var key;
                for(var i = 0,len = window.localStorage.length;i < len;i++){
                    key = window.localStorage.key(i);
                    if(!/.+\.expires$/.test(key)) {
                        result.push(key);
                    }
                }
            } else if(_isSupportUserData) { //IE7鍙婁互涓嬬増鏈紝閲囩敤UserData鏂瑰紡
                result = _getAllKeys();
            }

            return result;
        }
    };

})();
