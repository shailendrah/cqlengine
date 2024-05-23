/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(typeof dojo=="undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 6426 $".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.evalProp=function(_3,_4,_5){
if((!_4)||(!_3)){
return undefined;
}
if(!dj_undef(_3,_4)){
return _4[_3];
}
return (_5?(_4[_3]={}):undefined);
};
dojo.parseObjPath=function(_6,_7,_8){
var _9=(_7||dojo.global());
var _a=_6.split(".");
var _b=_a.pop();
for(var i=0,l=_a.length;i<l&&_9;i++){
_9=dojo.evalProp(_a[i],_9,_8);
}
return {obj:_9,prop:_b};
};
dojo.evalObjPath=function(_e,_f){
if(typeof _e!="string"){
return dojo.global();
}
if(_e.indexOf(".")==-1){
return dojo.evalProp(_e,dojo.global(),_f);
}
var ref=dojo.parseObjPath(_e,dojo.global(),_f);
if(ref){
return dojo.evalProp(ref.prop,ref.obj,_f);
}
return null;
};
dojo.errorToString=function(_11){
if(!dj_undef("message",_11)){
return _11.message;
}else{
if(!dj_undef("description",_11)){
return _11.description;
}else{
return _11;
}
}
};
dojo.raise=function(_12,_13){
if(_13){
_12=_12+": "+dojo.errorToString(_13);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_12);
}
}
catch(e){
}
throw _13||Error(_12);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_15){
return dj_global.eval?dj_global.eval(_15):eval(_15);
}
dojo.unimplemented=function(_16,_17){
var _18="'"+_16+"' not implemented";
if(_17!=null){
_18+=" "+_17;
}
dojo.raise(_18);
};
dojo.deprecated=function(_19,_1a,_1b){
var _1c="DEPRECATED: "+_19;
if(_1a){
_1c+=" "+_1a;
}
if(_1b){
_1c+=" -- will be removed in version: "+_1b;
}
dojo.debug(_1c);
};
dojo.render=(function(){
function vscaffold(_1d,_1e){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1d};
for(var i=0;i<_1e.length;i++){
tmp[_1e[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _21={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_21;
}else{
for(var _22 in _21){
if(typeof djConfig[_22]=="undefined"){
djConfig[_22]=_21[_22];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
var _25=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _26={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_27,_28){
this.modulePrefixes_[_27]={name:_27,value:_28};
},moduleHasPrefix:function(_29){
var mp=this.modulePrefixes_;
return Boolean(mp[_29]&&mp[_29].value);
},getModulePrefix:function(_2b){
if(this.moduleHasPrefix(_2b)){
return this.modulePrefixes_[_2b].value;
}
return _2b;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _26){
dojo.hostenv[_2c]=_26[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if(_2d.charAt(0)=="/"||_2d.match(/^\w+:/)){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2e?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _33=this.getText(uri,null,true);
if(!_33){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_36,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_3d){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3d]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_40){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_40]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_42){
var _43=_42.split(".");
for(var i=_43.length;i>0;i--){
var _45=_43.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_45)){
_43[0]="../"+_43[0];
}else{
var _46=this.getModulePrefix(_45);
if(_46!=_45){
_43.splice(0,i,_46);
break;
}
}
}
return _43;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_47,_48,_49){
if(!_47){
return;
}
_49=this._global_omit_module_check||_49;
var _4a=this.findModule(_47,false);
if(_4a){
return _4a;
}
if(dj_undef(_47,this.loading_modules_)){
this.addedToLoadingCount.push(_47);
}
this.loading_modules_[_47]=1;
var _4b=_47.replace(/\./g,"/")+".js";
var _4c=_47.split(".");
var _4d=this.getModuleSymbols(_47);
var _4e=((_4d[0].charAt(0)!="/")&&!_4d[0].match(/^\w+:/));
var _4f=_4d[_4d.length-1];
var ok;
if(_4f=="*"){
_47=_4c.slice(0,-1).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,!_49?_47:null);
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var _51=!_49?_47:null;
ok=this.loadPath(_4b,_51);
if(!ok&&!_48){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
}
}
if(!ok&&!_49){
dojo.raise("Could not load '"+_47+"'; last tried '"+_4b+"'");
}
}
if(!_49&&!this["isXDomain"]){
_4a=this.findModule(_47,false);
if(!_4a){
dojo.raise("symbol '"+_47+"' is not defined after loading '"+_4b+"'");
}
}
return _4a;
};
dojo.hostenv.startPackage=function(_52){
var _53=String(_52);
var _54=_53;
var _55=_52.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
_54=_55.join(".");
}
var _56=dojo.evalObjPath(_54,true);
this.loaded_modules_[_53]=_56;
this.loaded_modules_[_54]=_56;
return _56;
};
dojo.hostenv.findModule=function(_57,_58){
var lmn=String(_57);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_58){
dojo.raise("no loaded module named '"+_57+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_5a){
var _5b=_5a["common"]||[];
var _5c=_5a[dojo.hostenv.name_]?_5b.concat(_5a[dojo.hostenv.name_]||[]):_5b.concat(_5a["default"]||[]);
for(var x=0;x<_5c.length;x++){
var _5e=_5c[x];
if(_5e.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5e);
}else{
dojo.hostenv.loadModule(_5e);
}
}
};
dojo.require=function(_5f){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_60,_61){
var _62=arguments[0];
if((_62===true)||(_62=="common")||(_62&&dojo.render[_62].capable)){
var _63=[];
for(var i=1;i<arguments.length;i++){
_63.push(arguments[i]);
}
dojo.require.apply(dojo,_63);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_65){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_66,_67){
return dojo.hostenv.setModulePrefix(_66,_67);
};
dojo.setModulePrefix=function(_68,_69){
dojo.deprecated("dojo.setModulePrefix(\""+_68+"\", \""+_69+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_68,_69);
};
dojo.exists=function(obj,_6b){
var p=_6b.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_6e){
return _6e?_6e.toLowerCase():dojo.locale;
};
dojo.hostenv.searchLocalePath=function(_6f,_70,_71){
_6f=dojo.hostenv.normalizeLocale(_6f);
var _72=_6f.split("-");
var _73=[];
for(var i=_72.length;i>0;i--){
_73.push(_72.slice(0,i).join("-"));
}
_73.push(false);
if(_70){
_73.reverse();
}
for(var j=_73.length-1;j>=0;j--){
var loc=_73[j]||"ROOT";
var _77=_71(loc);
if(_77){
break;
}
}
};
dojo.hostenv.localesGenerated;
dojo.hostenv.registerNlsPrefix=function(){
dojo.registerModulePath("nls","nls");
};
dojo.hostenv.preloadLocalizations=function(){
if(dojo.hostenv.localesGenerated){
dojo.hostenv.registerNlsPrefix();
function preload(_78){
_78=dojo.hostenv.normalizeLocale(_78);
dojo.hostenv.searchLocalePath(_78,true,function(loc){
for(var i=0;i<dojo.hostenv.localesGenerated.length;i++){
if(dojo.hostenv.localesGenerated[i]==loc){
dojo["require"]("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _7b=djConfig.extraLocale||[];
for(var i=0;i<_7b.length;i++){
preload(_7b[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7d,_7e,_7f){
dojo.hostenv.preloadLocalizations();
var _80=[_7d,"nls",_7e].join(".");
var _81=dojo.hostenv.findModule(_80);
if(_81){
if(djConfig.localizationComplete&&_81._built){
return;
}
var _82=dojo.hostenv.normalizeLocale(_7f).replace("-","_");
var _83=_80+"."+_82;
if(dojo.hostenv.findModule(_83)){
return;
}
}
_81=dojo.hostenv.startPackage(_80);
var _84=dojo.hostenv.getModuleSymbols(_7d);
var _85=_84.concat("nls").join("/");
var _86;
dojo.hostenv.searchLocalePath(_7f,false,function(loc){
var _88=loc.replace("-","_");
var _89=_80+"."+_88;
var _8a=false;
if(!dojo.hostenv.findModule(_89)){
dojo.hostenv.startPackage(_89);
var _8b=[_85];
if(loc!="ROOT"){
_8b.push(loc);
}
_8b.push(_7e);
var _8c=_8b.join("/")+".js";
_8a=dojo.hostenv.loadPath(_8c,null,function(_8d){
var _8e=function(){
};
_8e.prototype=_86;
_81[_88]=new _8e();
for(var j in _8d){
_81[_88][j]=_8d[j];
}
});
}else{
_8a=true;
}
if(_8a&&_81[_88]){
_86=_81[_88];
}else{
_81[_88]=_86;
}
});
};
(function(){
var _90=djConfig.extraLocale;
if(_90){
if(!_90 instanceof Array){
_90=[_90];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_94){
req(m,b,_94);
if(_94){
return;
}
for(var i=0;i<_90.length;i++){
req(m,b,_90[i]);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _96=document.location.toString();
var _97=_96.split("?",2);
if(_97.length>1){
var _98=_97[1];
var _99=_98.split("&");
for(var x in _99){
var sp=_99[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _9d=document.getElementsByTagName("script");
var _9e=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_9d.length;i++){
var src=_9d[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_9e);
if(m){
var _a2=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_a2+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_a2;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_a2;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _aa=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_aa>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_aa+6,_aa+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _ac=window["document"];
var tdi=_ac["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b0=null;
var _b1=null;
try{
_b0=new XMLHttpRequest();
}
catch(e){
}
if(!_b0){
for(var i=0;i<3;++i){
var _b3=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b0=new ActiveXObject(_b3);
}
catch(e){
_b1=e;
}
if(_b0){
dojo.hostenv._XMLHTTP_PROGIDS=[_b3];
break;
}
}
}
if(!_b0){
return dojo.raise("XMLHTTP not available",_b1);
}
return _b0;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_b5,_b6){
if(!_b5){
this._blockAsync=true;
}
var _b7=this.getXmlhttpObject();
function isDocumentOk(_b8){
var _b9=_b8["status"];
return Boolean((!_b9)||((200<=_b9)&&(300>_b9))||(_b9==304));
}
if(_b5){
var _ba=this,_bb=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_b7.onreadystatechange=function(){
if(_bb){
gbl.clearTimeout(_bb);
_bb=null;
}
if(_ba._blockAsync||(xhr&&xhr._blockAsync)){
_bb=gbl.setTimeout(function(){
_b7.onreadystatechange.apply(this);
},10);
}else{
if(4==_b7.readyState){
if(isDocumentOk(_b7)){
_b5(_b7.responseText);
}
}
}
};
}
_b7.open("GET",uri,_b5?true:false);
try{
_b7.send(null);
if(_b5){
return null;
}
if(!isDocumentOk(_b7)){
var err=Error("Unable to load "+uri+" status:"+_b7.status);
err.status=_b7.status;
err.responseText=_b7.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_b6)&&(!_b5)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _b7.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_bf){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_bf);
}else{
try{
var _c0=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c0){
_c0=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_bf));
_c0.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_bf+"</div>");
}
catch(e2){
window.status=_bf;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_c2,_c3,fp,_c5){
var _c6=_c2["on"+_c3]||function(){
};
_c2["on"+_c3]=function(){
fp.apply(_c2,arguments);
_c6.apply(_c2,arguments);
};
return true;
}
function dj_load_init(e){
var _c8=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_c8!="domcontentloaded"&&_c8!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _c9=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_c9();
dojo.hostenv.modulesLoaded();
}else{
dojo.addOnLoad(_c9);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&!djConfig.delayMozLoadingFix)){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.attachEvent("onreadystatechange",function(e){
if(document.readyState=="complete"){
dj_load_init();
}
});
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _cb=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_cb=_cb.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_cb=_cb.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_cb.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _cc=new dojo.xml.Parse();
if(_cb.length>0){
for(var x=0;x<_cb.length;x++){
var _ce=document.getElementById(_cb[x]);
if(!_ce){
continue;
}
var _cf=_cc.parseElement(_ce,null,true);
dojo.widget.getParser().createComponents(_cf);
}
}else{
if(djConfig.parseWidgets){
var _cf=_cc.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_cf);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_d4,_d5){
dj_currentContext=_d4;
dj_currentDocument=_d5;
};
dojo._fireCallback=function(_d6,_d7,_d8){
if((_d7)&&((typeof _d6=="string")||(_d6 instanceof String))){
_d6=_d7[_d6];
}
return (_d7?_d6.apply(_d7,_d8||[]):_d6());
};
dojo.withGlobal=function(_d9,_da,_db,_dc){
var _dd;
var _de=dj_currentContext;
var _df=dj_currentDocument;
try{
dojo.setContext(_d9,_d9.document);
_dd=dojo._fireCallback(_da,_db,_dc);
}
finally{
dojo.setContext(_de,_df);
}
return _dd;
};
dojo.withDoc=function(_e0,_e1,_e2,_e3){
var _e4;
var _e5=dj_currentDocument;
try{
dj_currentDocument=_e0;
_e4=dojo._fireCallback(_e1,_e2,_e3);
}
finally{
dj_currentDocument=_e5;
}
return _e4;
};
}
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _e6=false;
var _e7=false;
var _e8=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_e6=true;
}else{
if(typeof this["load"]=="function"){
_e7=true;
}else{
if(window.widget){
_e8=true;
}
}
}
var _e9=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_e9.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_e6)&&(!_e8)){
_e9.push("browser_debug.js");
}
var _ea=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_ea=djConfig["baseLoaderUri"];
}
for(var x=0;x<_e9.length;x++){
var _ec=_ea+"src/"+_e9[x];
if(_e6||_e7){
load(_ec);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_ec+"'></scr"+"ipt>");
}
catch(e){
var _ed=document.createElement("script");
_ed.src=_ec;
document.getElementsByTagName("head")[0].appendChild(_ed);
}
}
}
})();
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_ee,_ef){
if(typeof _ef!="function"){
dojo.raise("dojo.inherits: superclass argument ["+_ef+"] must be a function (subclass: ["+_ee+"']");
}
_ee.prototype=new _ef();
_ee.prototype.constructor=_ee;
_ee.superclass=_ef.prototype;
_ee["super"]=_ef.prototype;
};
dojo.lang._mixin=function(obj,_f1){
var _f2={};
for(var x in _f1){
if((typeof _f2[x]=="undefined")||(_f2[x]!=_f1[x])){
obj[x]=_f1[x];
}
}
if(dojo.render.html.ie&&(typeof (_f1["toString"])=="function")&&(_f1["toString"]!=obj["toString"])&&(_f1["toString"]!=_f2["toString"])){
obj.toString=_f1.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_f5){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_f8,_f9){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_f8.prototype,arguments[i]);
}
return _f8;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_fc,_fd,_fe,_ff){
if(!dojo.lang.isArrayLike(_fc)&&dojo.lang.isArrayLike(_fd)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_fc;
_fc=_fd;
_fd=temp;
}
var _101=dojo.lang.isString(_fc);
if(_101){
_fc=_fc.split("");
}
if(_ff){
var step=-1;
var i=_fc.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_fc.length;
}
if(_fe){
while(i!=end){
if(_fc[i]===_fd){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_fc[i]==_fd){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_105,_106,_107){
return dojo.lang.find(_105,_106,_107,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_108,_109){
return dojo.lang.find(_108,_109)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
if(!it){
return false;
}
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction()&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.collections.Store");
dojo.collections.Store=function(_113){
var data=[];
this.keyField="Id";
this.get=function(){
return data;
};
this.getByKey=function(key){
for(var i=0;i<data.length;i++){
if(data[i].key==key){
return data[i];
}
}
return null;
};
this.getByIndex=function(idx){
return data[idx];
};
this.getData=function(){
var arr=[];
for(var i=0;i<data.length;i++){
arr.push(data[i].src);
}
return arr;
};
this.getDataByKey=function(key){
for(var i=0;i<data.length;i++){
if(data[i].key==key){
return data[i].src;
}
}
return null;
};
this.getDataByIndex=function(idx){
return data[idx].src;
};
this.update=function(obj,_11e,val){
var _120=_11e.split("."),i=0,o=obj,_123;
if(_120.length>1){
_123=_120.pop();
do{
if(_120[i].indexOf("()")>-1){
var temp=_120[i++].split("()")[0];
if(!o[temp]){
dojo.raise("dojo.collections.Store.getField(obj, '"+_123+"'): '"+temp+"' is not a property of the passed object.");
}else{
o=o[temp]();
}
}else{
o=o[_120[i++]];
}
}while(i<_120.length&&o!=null);
}else{
_123=_120[0];
}
obj[_123]=val;
this.onUpdateField(obj,_11e,val);
};
this.forEach=function(fn){
if(Array.forEach){
Array.forEach(data,fn,this);
}else{
for(var i=0;i<data.length;i++){
fn.call(this,data[i]);
}
}
};
this.forEachData=function(fn){
if(Array.forEach){
Array.forEach(this.getData(),fn,this);
}else{
var a=this.getData();
for(var i=0;i<a.length;i++){
fn.call(this,a[i]);
}
}
};
this.setData=function(arr){
data=[];
for(var i=0;i<arr.length;i++){
data.push({key:arr[i][this.keyField],src:arr[i]});
}
this.onSetData();
};
this.clearData=function(){
data=[];
this.onClearData();
};
this.addData=function(obj,key){
var k=key||obj[this.keyField];
if(this.getByKey(k)){
var o=this.getByKey(k);
o.src=obj;
}else{
var o={key:k,src:obj};
data.push(o);
}
this.onAddData(o);
};
this.addDataRange=function(arr){
var _131=[];
for(var i=0;i<arr.length;i++){
var k=arr[i][this.keyField];
if(this.getByKey(k)){
var o=this.getByKey(k);
o.src=obj;
}else{
var o={key:k,src:arr[i]};
data.push(o);
}
_131.push(o);
}
this.onAddDataRange(_131);
};
this.removeData=function(obj){
var idx=-1;
var o=null;
for(var i=0;i<data.length;i++){
if(data[i].src==obj){
idx=i;
o=data[i];
break;
}
}
this.onRemoveData(o);
if(idx>-1){
data.splice(idx,1);
}
};
this.removeDataByKey=function(key){
this.removeData(this.getDataByKey(key));
};
this.removeDataByIndex=function(idx){
this.removeData(this.getDataByIndex(idx));
};
if(_113&&_113.length&&_113[0]){
this.setData(_113);
}
};
dojo.extend(dojo.collections.Store,{getField:function(obj,_13c){
var _13d=_13c.split("."),i=0,o=obj;
do{
if(_13d[i].indexOf("()")>-1){
var temp=_13d[i++].split("()")[0];
if(!o[temp]){
dojo.raise("dojo.collections.Store.getField(obj, '"+_13c+"'): '"+temp+"' is not a property of the passed object.");
}else{
o=o[temp]();
}
}else{
o=o[_13d[i++]];
}
}while(i<_13d.length&&o!=null);
if(i<_13d.length){
dojo.raise("dojo.collections.Store.getField(obj, '"+_13c+"'): '"+_13c+"' is not a property of the passed object.");
}
return o;
},getFromHtml:function(meta,body,_143){
var rows=body.rows;
var ctor=function(row){
var obj={};
for(var i=0;i<meta.length;i++){
var o=obj;
var data=row.cells[i].innerHTML;
var p=meta[i].getField();
if(p.indexOf(".")>-1){
p=p.split(".");
while(p.length>1){
var pr=p.shift();
o[pr]={};
o=o[pr];
}
p=p[0];
}
var type=meta[i].getType();
if(type==String){
o[p]=data;
}else{
if(data){
o[p]=new type(data);
}else{
o[p]=new type();
}
}
}
return obj;
};
var arr=[];
for(var i=0;i<rows.length;i++){
var o=ctor(rows[i]);
if(_143){
_143(o,rows[i]);
}
arr.push(o);
}
return arr;
},onSetData:function(){
},onClearData:function(){
},onAddData:function(obj){
},onAddDataRange:function(arr){
},onRemoveData:function(obj){
},onUpdateField:function(obj,_155,val){
}});
dojo.provide("dojo.lang.array");
dojo.lang.has=function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
};
dojo.lang.isEmpty=function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _15b=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_15b++;
break;
}
}
return _15b==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
};
dojo.lang.map=function(arr,obj,_15f){
var _160=dojo.lang.isString(arr);
if(_160){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_15f)){
_15f=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_15f){
var _161=obj;
obj=_15f;
_15f=_161;
}
}
if(Array.map){
var _162=Array.map(arr,_15f,obj);
}else{
var _162=[];
for(var i=0;i<arr.length;++i){
_162.push(_15f.call(obj,arr[i]));
}
}
if(_160){
return _162.join("");
}else{
return _162;
}
};
dojo.lang.reduce=function(arr,_165,obj,_167){
var _168=_165;
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_168=_167.call(ob,_168,val);
});
return _168;
};
dojo.lang.forEach=function(_16b,_16c,_16d){
if(dojo.lang.isString(_16b)){
_16b=_16b.split("");
}
if(Array.forEach){
Array.forEach(_16b,_16c,_16d);
}else{
if(!_16d){
_16d=dj_global;
}
for(var i=0,l=_16b.length;i<l;i++){
_16c.call(_16d,_16b[i],i,_16b);
}
}
};
dojo.lang._everyOrSome=function(_170,arr,_172,_173){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_170?"every":"some"](arr,_172,_173);
}else{
if(!_173){
_173=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _176=_172.call(_173,arr[i],i,arr);
if(_170&&!_176){
return false;
}else{
if((!_170)&&(_176)){
return true;
}
}
}
return Boolean(_170);
}
};
dojo.lang.every=function(arr,_178,_179){
return this._everyOrSome(true,arr,_178,_179);
};
dojo.lang.some=function(arr,_17b,_17c){
return this._everyOrSome(false,arr,_17b,_17c);
};
dojo.lang.filter=function(arr,_17e,_17f){
var _180=dojo.lang.isString(arr);
if(_180){
arr=arr.split("");
}
var _181;
if(Array.filter){
_181=Array.filter(arr,_17e,_17f);
}else{
if(!_17f){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_17f=dj_global;
}
_181=[];
for(var i=0;i<arr.length;i++){
if(_17e.call(_17f,arr[i],i,arr)){
_181.push(arr[i]);
}
}
}
if(_180){
return _181.join("");
}else{
return _181;
}
};
dojo.lang.unnest=function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
};
dojo.lang.toArray=function(_186,_187){
var _188=[];
for(var i=_187||0;i<_186.length;i++){
_188.push(_186[i]);
}
return _188;
};
dojo.provide("dojo.gfx.color");
dojo.gfx.color.Color=function(r,g,b,a){
if(dojo.lang.isArray(r)){
this.r=r[0];
this.g=r[1];
this.b=r[2];
this.a=r[3]||1;
}else{
if(dojo.lang.isString(r)){
var rgb=dojo.gfx.color.extractRGB(r);
this.r=rgb[0];
this.g=rgb[1];
this.b=rgb[2];
this.a=g||1;
}else{
if(r instanceof dojo.gfx.color.Color){
this.r=r.r;
this.b=r.b;
this.g=r.g;
this.a=r.a;
}else{
this.r=r;
this.g=g;
this.b=b;
this.a=a;
}
}
}
};
dojo.gfx.color.Color.fromArray=function(arr){
return new dojo.gfx.color.Color(arr[0],arr[1],arr[2],arr[3]);
};
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_190){
if(_190){
return this.toRgba();
}else{
return [this.r,this.g,this.b];
}
},toRgba:function(){
return [this.r,this.g,this.b,this.a];
},toHex:function(){
return dojo.gfx.color.rgb2hex(this.toRgb());
},toCss:function(){
return "rgb("+this.toRgb().join()+")";
},toString:function(){
return this.toHex();
},blend:function(_191,_192){
var rgb=null;
if(dojo.lang.isArray(_191)){
rgb=_191;
}else{
if(_191 instanceof dojo.gfx.color.Color){
rgb=_191.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_191).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_192);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_196){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_196);
}
if(!_196){
_196=0;
}
_196=Math.min(Math.max(-1,_196),1);
_196=((_196+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_196));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_19b){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_19b));
};
dojo.gfx.color.extractRGB=function(_19c){
var hex="0123456789abcdef";
_19c=_19c.toLowerCase();
if(_19c.indexOf("rgb")==0){
var _19e=_19c.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_19e.splice(1,3);
return ret;
}else{
var _1a0=dojo.gfx.color.hex2rgb(_19c);
if(_1a0){
return _1a0;
}else{
return dojo.gfx.color.named[_19c]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _1a2="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_1a2+"]","g"),"")!=""){
return null;
}
if(hex.length==3){
rgb[0]=hex.charAt(0)+hex.charAt(0);
rgb[1]=hex.charAt(1)+hex.charAt(1);
rgb[2]=hex.charAt(2)+hex.charAt(2);
}else{
rgb[0]=hex.substring(0,2);
rgb[1]=hex.substring(2,4);
rgb[2]=hex.substring(4);
}
for(var i=0;i<rgb.length;i++){
rgb[i]=_1a2.indexOf(rgb[i].charAt(0))*16+_1a2.indexOf(rgb[i].charAt(1));
}
return rgb;
};
dojo.gfx.color.rgb2hex=function(r,g,b){
if(dojo.lang.isArray(r)){
g=r[1]||0;
b=r[2]||0;
r=r[0]||0;
}
var ret=dojo.lang.map([r,g,b],function(x){
x=new Number(x);
var s=x.toString(16);
while(s.length<2){
s="0"+s;
}
return s;
});
ret.unshift("#");
return ret.join("");
};
dojo.provide("dojo.gfx.color.hsl");
dojo.lang.extend(dojo.gfx.color.Color,{toHsl:function(){
return dojo.gfx.color.rgb2hsl(this.toRgb());
}});
dojo.gfx.color.rgb2hsl=function(r,g,b){
if(dojo.lang.isArray(r)){
b=r[2]||0;
g=r[1]||0;
r=r[0]||0;
}
r/=255;
g/=255;
b/=255;
var h=null;
var s=null;
var l=null;
var min=Math.min(r,g,b);
var max=Math.max(r,g,b);
var _1b3=max-min;
l=(min+max)/2;
s=0;
if((l>0)&&(l<1)){
s=_1b3/((l<0.5)?(2*l):(2-2*l));
}
h=0;
if(_1b3>0){
if((max==r)&&(max!=g)){
h+=(g-b)/_1b3;
}
if((max==g)&&(max!=b)){
h+=(2+(b-r)/_1b3);
}
if((max==b)&&(max!=r)){
h+=(4+(r-g)/_1b3);
}
h*=60;
}
h=(h==0)?360:Math.ceil((h/360)*255);
s=Math.ceil(s*255);
l=Math.ceil(l*255);
return [h,s,l];
};
dojo.gfx.color.hsl2rgb=function(h,s,l){
if(dojo.lang.isArray(h)){
l=h[2]||0;
s=h[1]||0;
h=h[0]||0;
}
h=(h/255)*360;
if(h==360){
h=0;
}
s=s/255;
l=l/255;
while(h<0){
h+=360;
}
while(h>360){
h-=360;
}
var r,g,b;
if(h<120){
r=(120-h)/60;
g=h/60;
b=0;
}else{
if(h<240){
r=0;
g=(240-h)/60;
b=(h-120)/60;
}else{
r=(h-240)/60;
g=0;
b=(360-h)/60;
}
}
r=Math.min(r,1);
g=Math.min(g,1);
b=Math.min(b,1);
r=2*s*r+(1-s);
g=2*s*g+(1-s);
b=2*s*b+(1-s);
if(l<0.5){
r=l*r;
g=l*g;
b=l*b;
}else{
r=(1-l)*r+2*l-1;
g=(1-l)*g+2*l-1;
b=(1-l)*b+2*l-1;
}
r=Math.ceil(r*255);
g=Math.ceil(g*255);
b=Math.ceil(b*255);
return [r,g,b];
};
dojo.gfx.color.hsl2hex=function(h,s,l){
var rgb=dojo.gfx.color.hsl2rgb(h,s,l);
return dojo.gfx.color.rgb2hex(rgb[0],rgb[1],rgb[2]);
};
dojo.gfx.color.hex2hsl=function(hex){
var rgb=dojo.gfx.color.hex2rgb(hex);
return dojo.gfx.color.rgb2hsl(rgb[0],rgb[1],rgb[2]);
};
dojo.provide("dojo.charting.Axis");
dojo.charting.Axis=function(_1c0,_1c1,_1c2){
var id="dojo-charting-axis-"+dojo.charting.Axis.count++;
this.getId=function(){
return id;
};
this.setId=function(key){
id=key;
};
this.scale=_1c1||"linear";
this.label=_1c0||"";
this.showLabel=true;
this.showLabels=true;
this.showLines=false;
this.showTicks=false;
this.range={upper:0,lower:0};
this.origin="min";
this.labels=_1c2||[];
this._labels=[];
this.nodes={main:null,axis:null,label:null,labels:null,lines:null,ticks:null};
};
dojo.charting.Axis.count=0;
dojo.extend(dojo.charting.Axis,{getCoord:function(val,_1c6,plot){
val=parseFloat(val,10);
var area=_1c6.getArea();
if(plot.axisX==this){
var _1c9=0-this.range.lower;
var min=this.range.lower+_1c9;
var max=this.range.upper+_1c9;
val+=_1c9;
return (val*((area.right-area.left)/max))+area.left;
}else{
var max=this.range.upper;
var min=this.range.lower;
var _1c9=0;
if(min<0){
_1c9+=Math.abs(min);
}
max+=_1c9;
min+=_1c9;
val+=_1c9;
var pmin=area.bottom;
var pmax=area.top;
return (((pmin-pmax)/(max-min))*(max-val))+pmax;
}
},initializeOrigin:function(_1ce,_1cf){
if(isNaN(this.origin)){
if(this.origin.toLowerCase()=="max"){
this.origin=_1ce.range[(_1cf=="y")?"upper":"lower"];
}else{
if(this.origin.toLowerCase()=="min"){
this.origin=_1ce.range[(_1cf=="y")?"lower":"upper"];
}else{
this.origin=0;
}
}
}
},initializeLabels:function(){
if(this.labels.length==0){
this.showLabels=false;
this.showLines=false;
this.showTicks=false;
}else{
if(this.labels[0].label&&this.labels[0].value!=null){
for(var i=0;i<this.labels.length;i++){
this._labels.push(this.labels[i]);
}
}else{
if(!isNaN(this.labels[0])){
for(var i=0;i<this.labels.length;i++){
this._labels.push({label:this.labels[i],value:this.labels[i]});
}
}else{
var a=[];
for(var i=0;i<this.labels.length;i++){
a.push(this.labels[i]);
}
var s=a.shift();
this._labels.push({label:s,value:this.range.lower});
if(a.length>0){
var s=a.pop();
this._labels.push({label:s,value:this.range.upper});
}
if(a.length>0){
var _1d3=this.range.upper-this.range.lower;
var step=_1d3/(this.labels.length-1);
for(var i=1;i<=a.length;i++){
this._labels.push({label:a[i-1],value:this.range.lower+(step*i)});
}
}
}
}
}
},initialize:function(_1d5,plot,_1d7,_1d8){
this.destroy();
this.initializeOrigin(_1d7,_1d8);
this.initializeLabels();
var node=this.render(_1d5,plot,_1d7,_1d8);
return node;
},destroy:function(){
for(var p in this.nodes){
while(this.nodes[p]&&this.nodes[p].childNodes.length>0){
this.nodes[p].removeChild(this.nodes[p].childNodes[0]);
}
if(this.nodes[p]&&this.nodes[p].parentNode){
this.nodes[p].parentNode.removeChild(this.nodes[p]);
}
this.nodes[p]=null;
}
}});
dojo["requireIf"](dojo.render.svg.capable,"dojo.charting.svg.Axis");
dojo["requireIf"](!dojo.render.svg.capable&&dojo.render.vml.capable,"dojo.charting.vml.Axis");
dojo.provide("dojo.charting.Plotters");
dojo["requireIf"](dojo.render.svg.capable,"dojo.charting.svg.Plotters");
dojo["requireIf"](!dojo.render.svg.capable&&dojo.render.vml.capable,"dojo.charting.vml.Plotters");
dojo.provide("dojo.charting.Series");
dojo.charting.Series=function(_1db){
var args=_1db||{length:1};
this.dataSource=args.dataSource||null;
this.bindings={};
this.color=args.color;
this.label=args.label;
if(args.bindings){
for(var p in args.bindings){
this.addBinding(p,args.bindings[p]);
}
}
};
dojo.extend(dojo.charting.Series,{bind:function(src,_1df){
this.dataSource=src;
this.bindings=_1df;
},addBinding:function(name,_1e1){
this.bindings[name]=_1e1;
},evaluate:function(_1e2){
var ret=[];
var a=this.dataSource.getData();
var l=a.length;
var _1e6=0;
var end=l;
if(_1e2){
if(_1e2.from){
_1e6=Math.max(_1e2.from,0);
if(_1e2.to){
end=Math.min(_1e2.to,end);
}
}else{
if(_1e2.length){
if(_1e2.length<0){
_1e6=Math.max((end+length),0);
}else{
end=Math.min((_1e6+length),end);
}
}
}
}
for(var i=_1e6;i<end;i++){
var o={src:a[i],series:this};
for(var p in this.bindings){
o[p]=this.dataSource.getField(a[i],this.bindings[p]);
}
ret.push(o);
}
if(typeof (ret[0].x)!="undefined"){
ret.sort(function(a,b){
if(a.x>b.x){
return 1;
}
if(a.x<b.x){
return -1;
}
return 0;
});
}
return ret;
},trends:{createRange:function(_1ed,len){
var idx=_1ed.length-1;
var _1f0=(len||_1ed.length);
return {"index":idx,"length":_1f0,"start":Math.max(idx-_1f0,0)};
},mean:function(_1f1,len){
var _1f3=this.createRange(_1f1,len);
if(_1f3.index<0){
return 0;
}
var _1f4=0;
var _1f5=0;
for(var i=_1f3.index;i>=_1f3.start;i--){
_1f4+=_1f1[i].y;
_1f5++;
}
_1f4/=Math.max(_1f5,1);
return _1f4;
},variance:function(_1f7,len){
var _1f9=this.createRange(_1f7,len);
if(_1f9.index<0){
return 0;
}
var _1fa=0;
var _1fb=0;
var _1fc=0;
for(var i=_1f9.index;i>=_1f9.start;i--){
_1fa+=_1f7[i].y;
_1fb+=Math.pow(_1f7[i].y,2);
_1fc++;
}
return (_1fb/_1fc)-Math.pow(_1fa/_1fc,2);
},standardDeviation:function(_1fe,len){
return Math.sqrt(this.getVariance(_1fe,len));
},max:function(_200,len){
var _202=this.createRange(_200,len);
if(_202.index<0){
return 0;
}
var max=Number.MIN_VALUE;
for(var i=_202.index;i>=_202.start;i--){
max=Math.max(_200[i].y,max);
}
return max;
},min:function(_205,len){
var _207=this.createRange(_205,len);
if(_207.index<0){
return 0;
}
var min=Number.MAX_VALUE;
for(var i=_207.index;i>=_207.start;i--){
min=Math.min(_205[i].y,min);
}
return min;
},median:function(_20a,len){
var _20c=this.createRange(_20a,len);
if(_20c.index<0){
return 0;
}
var a=[];
for(var i=_20c.index;i>=_20c.start;i--){
var b=false;
for(var j=0;j<a.length;j++){
if(_20a[i].y==a[j]){
b=true;
break;
}
}
if(!b){
a.push(_20a[i].y);
}
}
a.sort();
if(a.length>0){
return a[Math.ceil(a.length/2)];
}
return 0;
},mode:function(_211,len){
var _213=this.createRange(_211,len);
if(_213.index<0){
return 0;
}
var o={};
var ret=0;
var _216=Number.MIN_VALUE;
for(var i=_213.index;i>=_213.start;i--){
if(!o[_211[i].y]){
o[_211[i].y]=1;
}else{
o[_211[i].y]++;
}
}
for(var p in o){
if(_216<o[p]){
_216=o[p];
ret=p;
}
}
return ret;
}}});
dojo.provide("dojo.charting.Plot");
dojo.charting.RenderPlotSeries={Singly:"single",Grouped:"grouped"};
dojo.charting.Plot=function(_219,_21a,_21b){
var id="dojo-charting-plot-"+dojo.charting.Plot.count++;
this.getId=function(){
return id;
};
this.setId=function(key){
id=key;
};
this.axisX=null;
this.axisY=null;
this.series=[];
this.dataNode=null;
this.renderType=dojo.charting.RenderPlotSeries.Singly;
if(_219){
this.setAxis(_219,"x");
}
if(_21a){
this.setAxis(_21a,"y");
}
if(_21b){
for(var i=0;i<_21b.length;i++){
this.addSeries(_21b[i]);
}
}
};
dojo.charting.Plot.count=0;
dojo.extend(dojo.charting.Plot,{addSeries:function(_21f,_220){
if(_21f.plotter){
this.series.push(_21f);
}else{
this.series.push({data:_21f,plotter:_220||dojo.charting.Plotters["Default"]});
}
},setAxis:function(axis,_222){
if(_222.toLowerCase()=="x"){
this.axisX=axis;
}else{
if(_222.toLowerCase()=="y"){
this.axisY=axis;
}
}
},getRanges:function(){
var xmin,xmax,ymin,ymax;
xmin=ymin=Number.MAX_VALUE;
xmax=ymax=Number.MIN_VALUE;
for(var i=0;i<this.series.length;i++){
var _228=this.series[i].data.evaluate();
for(var j=0;j<_228.length;j++){
var comp=_228[j];
xmin=Math.min(comp.x,xmin);
ymin=Math.min(comp.y,ymin);
xmax=Math.max(comp.x,xmax);
ymax=Math.max(comp.y,ymax);
}
}
return {x:{upper:xmax,lower:xmin},y:{upper:ymax,lower:ymin},toString:function(){
return "[ x:"+xmax+" - "+xmin+", y:"+ymax+" - "+ymin+"]";
}};
},destroy:function(){
var node=this.dataNode;
while(node&&node.childNodes&&node.childNodes.length>0){
node.removeChild(node.childNodes[0]);
}
this.dataNode=null;
}});
dojo.provide("dojo.charting.PlotArea");
dojo.charting.PlotArea=function(){
var id="dojo-charting-plotarea-"+dojo.charting.PlotArea.count++;
this.getId=function(){
return id;
};
this.setId=function(key){
id=key;
};
this.areaType="standard";
this.plots=[];
this.size={width:600,height:400};
this.padding={top:10,right:10,bottom:20,left:20};
this.nodes={main:null,area:null,background:null,axes:null,plots:null};
this._color={h:140,s:120,l:120,step:27};
};
dojo.charting.PlotArea.count=0;
dojo.extend(dojo.charting.PlotArea,{nextColor:function(){
var rgb=dojo.gfx.color.hsl2rgb(this._color.h,this._color.s,this._color.l);
this._color.h=(this._color.h+this._color.step)%360;
while(this._color.h<140){
this._color.h+=this._color.step;
}
return dojo.gfx.color.rgb2hex(rgb[0],rgb[1],rgb[2]);
},getArea:function(){
return {left:this.padding.left,right:this.size.width-this.padding.right,top:this.padding.top,bottom:this.size.height-this.padding.bottom,toString:function(){
var a=[this.top,this.right,this.bottom,this.left];
return "["+a.join()+"]";
}};
},getAxes:function(){
var axes={};
for(var i=0;i<this.plots.length;i++){
var plot=this.plots[i];
axes[plot.axisX.getId()]={axis:plot.axisX,drawAgainst:plot.axisY,plot:plot,plane:"x"};
axes[plot.axisY.getId()]={axis:plot.axisY,drawAgainst:plot.axisX,plot:plot,plane:"y"};
}
return axes;
},getLegendInfo:function(){
var a=[];
for(var i=0;i<this.plots.length;i++){
for(var j=0;j<this.plots[i].series.length;j++){
var data=this.plots[i].series[j].data;
a.push({label:data.label,color:data.color});
}
}
return a;
},setAxesRanges:function(){
var _237={};
var axes={};
for(var i=0;i<this.plots.length;i++){
var plot=this.plots[i];
var _237=plot.getRanges();
var x=_237.x;
var y=_237.y;
var ax,ay;
if(!axes[plot.axisX.getId()]){
axes[plot.axisX.getId()]=plot.axisX;
_237[plot.axisX.getId()]={upper:x.upper,lower:x.lower};
}
ax=_237[plot.axisX.getId()];
ax.upper=Math.max(ax.upper,x.upper);
ax.lower=Math.min(ax.lower,x.lower);
if(!axes[plot.axisY.getId()]){
axes[plot.axisY.getId()]=plot.axisY;
_237[plot.axisY.getId()]={upper:y.upper,lower:y.lower};
}
ay=_237[plot.axisY.getId()];
ay.upper=Math.max(ay.upper,y.upper);
ay.lower=Math.min(ay.lower,y.lower);
}
for(var p in axes){
axes[p].range=_237[p];
}
},render:function(_240,_241){
if(!this.nodes.main||!this.nodes.area||!this.nodes.background||!this.nodes.plots||!this.nodes.axes){
this.initialize();
}
for(var i=0;i<this.plots.length;i++){
var plot=this.plots[i];
this.nodes.plots.removeChild(plot.dataNode);
var _244=this.initializePlot(plot);
switch(plot.renderType){
case dojo.charting.RenderPlotSeries.Grouped:
_244.appendChild(plot.series[0].plotter(this,plot,_240,_241));
break;
case dojo.charting.RenderPlotSeries.Singly:
default:
for(var j=0;j<plot.series.length;j++){
var _246=plot.series[j];
var data=_246.data.evaluate(_240);
_244.appendChild(_246.plotter(data,this,plot,_241));
}
}
this.nodes.plots.appendChild(_244);
}
},destroy:function(){
for(var i=0;i<this.plots.length;i++){
this.plots[i].destroy();
}
for(var p in this.nodes){
var node=this.nodes[p];
if(!node){
continue;
}
if(!node.childNodes){
continue;
}
while(node.childNodes.length>0){
node.removeChild(node.childNodes[0]);
}
this.nodes[p]=null;
}
}});
dojo["requireIf"](dojo.render.svg.capable,"dojo.charting.svg.PlotArea");
dojo["requireIf"](!dojo.render.svg.capable&&dojo.render.vml.capable,"dojo.charting.vml.PlotArea");
dojo.provide("dojo.charting.Chart");
dojo.charting.Chart=function(node,_24c,_24d){
this.node=node||null;
this.title=_24c||"Chart";
this.description=_24d||"";
this.plotAreas=[];
};
dojo.extend(dojo.charting.Chart,{addPlotArea:function(obj,_24f){
if(obj.x&&!obj.left){
obj.left=obj.x;
}
if(obj.y&&!obj.top){
obj.top=obj.y;
}
this.plotAreas.push(obj);
if(_24f){
this.render();
}
},onInitialize:function(_250){
},onRender:function(_251){
},onDestroy:function(_252){
},initialize:function(){
if(!this.node){
dojo.raise("dojo.charting.Chart.initialize: there must be a root node defined for the Chart.");
}
this.destroy();
this.render();
this.onInitialize(this);
},render:function(){
if(this.node.style.position!="absolute"){
this.node.style.position="relative";
}
for(var i=0;i<this.plotAreas.length;i++){
var area=this.plotAreas[i].plotArea;
var node=area.initialize();
node.style.position="absolute";
node.style.top=this.plotAreas[i].top+"px";
node.style.left=this.plotAreas[i].left+"px";
this.node.appendChild(node);
area.render();
}
},destroy:function(){
for(var i=0;i<this.plotAreas.length;i++){
this.plotAreas[i].plotArea.destroy();
}
while(this.node&&this.node.childNodes&&this.node.childNodes.length>0){
this.node.removeChild(this.node.childNodes[0]);
}
}});
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_257,_258){
var fcn=(dojo.lang.isString(_258)?_257[_258]:_258)||function(){
};
return function(){
return fcn.apply(_257,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_25a,_25b,_25c){
var nso=(_25b||dojo.lang.anon);
if((_25c)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_25a){
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_25a;
return ret;
};
dojo.lang.forward=function(_260){
return function(){
return this[_260].apply(this,arguments);
};
};
dojo.lang.curry=function(ns,func){
var _263=[];
ns=ns||dj_global;
if(dojo.lang.isString(func)){
func=ns[func];
}
for(var x=2;x<arguments.length;x++){
_263.push(arguments[x]);
}
var _265=(func["__preJoinArity"]||func.length)-_263.length;
function gather(_266,_267,_268){
var _269=_268;
var _26a=_267.slice(0);
for(var x=0;x<_266.length;x++){
_26a.push(_266[x]);
}
_268=_268-_266.length;
if(_268<=0){
var res=func.apply(ns,_26a);
_268=_269;
return res;
}else{
return function(){
return gather(arguments,_26a,_268);
};
}
}
return gather([],_263,_265);
};
dojo.lang.curryArguments=function(ns,func,args,_270){
var _271=[];
var x=_270||0;
for(x=_270;x<args.length;x++){
_271.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[ns,func].concat(_271));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_277,_278){
if(!farr.length){
if(typeof _278=="function"){
_278();
}
return;
}
if((typeof _277=="undefined")&&(typeof cb=="number")){
_277=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_277){
_277=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_277,_278);
},_277);
};
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_27f,_280){
var out="";
for(var i=0;i<_27f;i++){
out+=str;
if(_280&&i<_27f-1){
out+=_280;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_28e,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _28e.replace(/\%\{(\w+)\}/g,function(_291,key){
if(typeof (map[key])!="undefined"&&map[key]!=null){
return map[key];
}
dojo.raise("Substitution not found: "+key);
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
if(arguments.length==0){
str=this;
}
var _294=str.split(" ");
for(var i=0;i<_294.length;i++){
_294[i]=_294[i].charAt(0).toUpperCase()+_294[i].substring(1);
}
return _294.join(" ");
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _299=escape(str);
var _29a,re=/%u([0-9A-F]{4})/i;
while((_29a=_299.match(re))){
var num=Number("0x"+_29a[1]);
var _29d=escape("&#"+num+";");
ret+=_299.substring(0,_29a.index)+_29d;
_299=_299.substring(_29a.index+_29a[0].length);
}
ret+=_299.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_2a2){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_2a2){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str){
return str.replace(/\\/gm,"\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm,"\\$1");
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_2ab){
if(_2ab){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_2af,_2b0){
if(_2b0){
str=str.toLowerCase();
_2af=_2af.toLowerCase();
}
return str.indexOf(_2af)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_2b6){
if(_2b6=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_2b6=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_2b8){
var _2b9=[];
for(var i=0,_2bb=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_2b8){
_2b9.push(str.substring(_2bb,i));
_2bb=i+1;
}
}
_2b9.push(str.substr(_2bb));
return _2b9;
};
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_2bc){
this.pairs=[];
this.returnWrappers=_2bc||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_2be,wrap,_2c0,_2c1){
var type=(_2c1)?"unshift":"push";
this.pairs[type]([name,_2be,wrap,_2c0]);
},match:function(){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[1].apply(this,arguments)){
if((pair[3])||(this.returnWrappers)){
return pair[2];
}else{
return pair[2].apply(this,arguments);
}
}
}
throw new Error("No match found");
},unregister:function(name){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[0]==name){
this.pairs.splice(i,1);
return true;
}
}
return false;
}});
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_2c9,wrap,_2cb){
dojo.json.jsonRegistry.register(name,_2c9,wrap,_2cb);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _2ce=typeof (o);
if(_2ce=="undefined"){
return "undefined";
}else{
if((_2ce=="number")||(_2ce=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_2ce=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _2d0;
if(typeof (o.__json__)=="function"){
_2d0=o.__json__();
if(o!==_2d0){
return me(_2d0);
}
}
if(typeof (o.json)=="function"){
_2d0=o.json();
if(o!==_2d0){
return me(_2d0);
}
}
if(_2ce!="function"&&typeof (o.length)=="number"){
var res=[];
for(var i=0;i<o.length;i++){
var val=me(o[i]);
if(typeof (val)!="string"){
val="undefined";
}
res.push(val);
}
return "["+res.join(",")+"]";
}
try{
window.o=o;
_2d0=dojo.json.jsonRegistry.match(o);
return me(_2d0);
}
catch(e){
}
if(_2ce=="function"){
return null;
}
res=[];
for(var k in o){
var _2d5;
if(typeof (k)=="number"){
_2d5="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_2d5=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_2d5+":"+val);
}
return "{"+res.join(",")+"}";
}};
dojo.provide("dojo.string");
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_2d7){
var _2d8=window,_2d9=2;
if(!dojo.lang.isFunction(func)){
_2d8=func;
func=_2d7;
_2d7=arguments[2];
_2d9++;
}
if(dojo.lang.isString(func)){
func=_2d8[func];
}
var args=[];
for(var i=_2d9;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_2d8,args);
},_2d7);
};
dojo.lang.clearTimeout=function(_2dc){
dojo.global().clearTimeout(_2dc);
};
dojo.lang.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.lang.shallowCopy=function(obj,deep){
var i,ret;
if(obj===null){
return null;
}
if(dojo.lang.isObject(obj)){
ret=new obj.constructor();
for(i in obj){
if(dojo.lang.isUndefined(ret[i])){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}
}else{
if(dojo.lang.isArray(obj)){
ret=[];
for(i=0;i<obj.length;i++){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}else{
ret=obj;
}
}
return ret;
};
dojo.lang.firstValued=function(){
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]!="undefined"){
return arguments[i];
}
}
return undefined;
};
dojo.lang.getObjPathValue=function(_2e5,_2e6,_2e7){
with(dojo.parseObjPath(_2e5,_2e6,_2e7)){
return dojo.evalProp(prop,obj,_2e7);
}
};
dojo.lang.setObjPathValue=function(_2e8,_2e9,_2ea,_2eb){
if(arguments.length<4){
_2eb=true;
}
with(dojo.parseObjPath(_2e8,_2ea,_2eb)){
if(obj&&(_2eb||(prop in obj))){
obj[prop]=_2e9;
}
}
};
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_2ed,_2ee,_2ef){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_2ed){
this.mimetype=_2ed;
}
if(_2ee){
this.transport=_2ee;
}
if(arguments.length>=4){
this.changeUrl=_2ef;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_2f2,_2f3){
},error:function(type,_2f5,_2f6,_2f7){
},timeout:function(type,_2f9,_2fa,_2fb){
},handle:function(type,data,_2fe,_2ff){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_300){
if(_300["url"]){
_300.url=_300.url.toString();
}
if(_300["formNode"]){
_300.formNode=dojo.byId(_300.formNode);
}
if(!_300["method"]&&_300["formNode"]&&_300["formNode"].method){
_300.method=_300["formNode"].method;
}
if(!_300["handle"]&&_300["handler"]){
_300.handle=_300.handler;
}
if(!_300["load"]&&_300["loaded"]){
_300.load=_300.loaded;
}
if(!_300["changeUrl"]&&_300["changeURL"]){
_300.changeUrl=_300.changeURL;
}
_300.encoding=dojo.lang.firstValued(_300["encoding"],djConfig["bindEncoding"],"");
_300.sendTransport=dojo.lang.firstValued(_300["sendTransport"],djConfig["ioSendTransport"],false);
var _301=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_300[fn]&&_301(_300[fn])){
continue;
}
if(_300["handle"]&&_301(_300["handle"])){
_300[fn]=_300.handle;
}
}
dojo.lang.mixin(this,_300);
}});
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_308){
if(!(_308 instanceof dojo.io.Request)){
try{
_308=new dojo.io.Request(_308);
}
catch(e){
dojo.debug(e);
}
}
var _309="";
if(_308["transport"]){
_309=_308["transport"];
if(!this[_309]){
dojo.io.sendBindError(_308,"No dojo.io.bind() transport with name '"+_308["transport"]+"'.");
return _308;
}
if(!this[_309].canHandle(_308)){
dojo.io.sendBindError(_308,"dojo.io.bind() transport with name '"+_308["transport"]+"' cannot handle this type of request.");
return _308;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_308))){
_309=tmp;
break;
}
}
if(_309==""){
dojo.io.sendBindError(_308,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _308;
}
}
this[_309].bind(_308);
_308.bindSuccess=true;
return _308;
};
dojo.io.sendBindError=function(_30c,_30d){
if((typeof _30c.error=="function"||typeof _30c.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _30e=new dojo.io.Error(_30d);
setTimeout(function(){
_30c[(typeof _30c.error=="function")?"error":"handle"]("error",_30e,null,_30c);
},50);
}else{
dojo.raise(_30d);
}
};
dojo.io.queueBind=function(_30f){
if(!(_30f instanceof dojo.io.Request)){
try{
_30f=new dojo.io.Request(_30f);
}
catch(e){
dojo.debug(e);
}
}
var _310=_30f.load;
_30f.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_310.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _312=_30f.error;
_30f.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_312.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_30f);
dojo.io._dispatchNextQueueBind();
return _30f;
};
dojo.io._dispatchNextQueueBind=function(){
if(!dojo.io._queueBindInFlight){
dojo.io._queueBindInFlight=true;
if(dojo.io._bindQueue.length>0){
dojo.io.bind(dojo.io._bindQueue.shift());
}else{
dojo.io._queueBindInFlight=false;
}
}
};
dojo.io._bindQueue=[];
dojo.io._queueBindInFlight=false;
dojo.io.argsFromMap=function(map,_315,last){
var enc=/utf/i.test(_315||"")?encodeURIComponent:dojo.string.encodeAscii;
var _318=[];
var _319=new Object();
for(var name in map){
var _31b=function(elt){
var val=enc(name)+"="+enc(elt);
_318[(last==name)?"push":"unshift"](val);
};
if(!_319[name]){
var _31e=map[name];
if(dojo.lang.isArray(_31e)){
dojo.lang.forEach(_31e,_31b);
}else{
_31b(_31e);
}
}
}
return _318.join("&");
};
dojo.io.setIFrameSrc=function(_31f,src,_321){
try{
var r=dojo.render.html;
if(!_321){
if(r.safari){
_31f.location=src;
}else{
frames[_31f.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_31f.contentWindow.document;
}else{
if(r.safari){
idoc=_31f.document;
}else{
idoc=_31f.contentWindow;
}
}
if(!idoc){
_31f.location=src;
return;
}else{
idoc.location.replace(src);
}
}
}
catch(e){
dojo.debug(e);
dojo.debug("setIFrameSrc: "+e);
}
};
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(E){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _325=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_325.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_327,_328){
var node=_327.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_328&&node&&node.tagName&&node.tagName.toLowerCase()!=_328.toLowerCase()){
node=dojo.dom.nextElement(node,_328);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_32a,_32b){
var node=_32a.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_32b&&node&&node.tagName&&node.tagName.toLowerCase()!=_32b.toLowerCase()){
node=dojo.dom.prevElement(node,_32b);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_32e){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_32e&&_32e.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_32e);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_330){
if(!node){
return null;
}
if(_330){
_330=_330.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_330&&_330.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_330);
}
return node;
};
dojo.dom.moveChildren=function(_331,_332,trim){
var _334=0;
if(trim){
while(_331.hasChildNodes()&&_331.firstChild.nodeType==dojo.dom.TEXT_NODE){
_331.removeChild(_331.firstChild);
}
while(_331.hasChildNodes()&&_331.lastChild.nodeType==dojo.dom.TEXT_NODE){
_331.removeChild(_331.lastChild);
}
}
while(_331.hasChildNodes()){
_332.appendChild(_331.firstChild);
_334++;
}
return _334;
};
dojo.dom.copyChildren=function(_335,_336,trim){
var _338=_335.cloneNode(true);
return this.moveChildren(_338,_336,trim);
};
dojo.dom.removeChildren=function(node){
var _33a=node.childNodes.length;
while(node.hasChildNodes()){
node.removeChild(node.firstChild);
}
return _33a;
};
dojo.dom.replaceChildren=function(node,_33c){
dojo.dom.removeChildren(node);
node.appendChild(_33c);
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_33f,_340){
var _341=[];
var _342=(_33f&&(_33f instanceof Function||typeof _33f=="function"));
while(node){
if(!_342||_33f(node)){
_341.push(node);
}
if(_340&&_341.length>0){
return _341[0];
}
node=node.parentNode;
}
if(_340){
return null;
}
return _341;
};
dojo.dom.getAncestorsByTag=function(node,tag,_345){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_345);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_34a,_34b){
if(_34b&&node){
node=node.parentNode;
}
while(node){
if(node==_34a){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _34e=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _34f=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_34f.length;i++){
try{
doc=new ActiveXObject(_34f[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_34e.implementation)&&(_34e.implementation.createDocument)){
doc=_34e.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_352){
if(!_352){
_352="text/xml";
}
if(!dj_undef("DOMParser")){
var _353=new DOMParser();
return _353.parseFromString(str,_352);
}else{
if(!dj_undef("ActiveXObject")){
var _354=dojo.dom.createDocument();
if(_354){
_354.async=false;
_354.loadXML(str);
return _354;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _355=dojo.doc();
if(_355.createElement){
var tmp=_355.createElement("xml");
tmp.innerHTML=str;
if(_355.implementation&&_355.implementation.createDocument){
var _357=_355.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_357.importNode(tmp.childNodes.item(i),true);
}
return _357;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_35a){
if(_35a.firstChild){
_35a.insertBefore(node,_35a.firstChild);
}else{
_35a.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_35d){
if(_35d!=true&&(node===ref||node.nextSibling===ref)){
return false;
}
var _35e=ref.parentNode;
_35e.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_361){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_361!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_361);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_365){
if((!node)||(!ref)||(!_365)){
return false;
}
switch(_365.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_367,_368){
var _369=_367.childNodes;
if(!_369.length){
_367.appendChild(node);
return true;
}
var _36a=null;
for(var i=0;i<_369.length;i++){
var _36c=_369.item(i)["getAttribute"]?parseInt(_369.item(i).getAttribute("dojoinsertionindex")):-1;
if(_36c<_368){
_36a=_369.item(i);
}
}
if(_36a){
return dojo.dom.insertAfter(node,_36a);
}else{
return dojo.dom.insertBefore(node,_369.item(0));
}
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _36f=dojo.doc();
dojo.dom.replaceChildren(node,_36f.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _370="";
if(node==null){
return _370;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_370+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_370+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _370;
}
};
dojo.dom.hasParent=function(node){
return node&&node.parentNode&&dojo.dom.isNode(node.parentNode);
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_376,_377,_378){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_376,_377,_378);
}else{
var _379=elem.ownerDocument;
var _37a=_379.createNode(2,_377,_376);
_37a.nodeValue=_378;
elem.setAttributeNode(_37a);
}
};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:window.location.href,initialHash:window.location.hash,moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
dojo.body().appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if(args["changeUrl"]){
hash="#"+((args["changeUrl"]!==true)?args["changeUrl"]:(new Date()).getTime());
if(this.historyStack.length==0&&this.initialState.urlHash==hash){
this.initialState=this._createState(url,args,hash);
return;
}else{
if(this.historyStack.length>0&&this.historyStack[this.historyStack.length-1].urlHash==hash){
this.historyStack[this.historyStack.length-1]=this._createState(url,args,hash);
return;
}
}
this.changingUrl=true;
setTimeout("window.location.href = '"+hash+"'; dojo.undo.browser.changingUrl = false;",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
url=this._loadIframeHistory();
var _37f=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_381){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_37f.apply(this,[_381]);
};
if(args["back"]){
args.back=tcb;
}else{
if(args["backButton"]){
args.backButton=tcb;
}else{
if(args["handle"]){
args.handle=tcb;
}
}
}
var _382=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_384){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_382){
_382.apply(this,[_384]);
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}else{
if(args["handle"]){
args.handle=tfw;
}
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.undo.browser.checkLocation();",200);
}
}
}
}else{
url=this._loadIframeHistory();
}
this.historyStack.push(this._createState(url,args,hash));
},checkLocation:function(){
if(!this.changingUrl){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash||window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
}
},iframeLoaded:function(evt,_387){
if(!dojo.render.html.opera){
var _388=this._getUrlQuery(_387.href);
if(_388==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_388==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_388==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _389=this.historyStack.pop();
if(!_389){
return;
}
var last=this.historyStack[this.historyStack.length-1];
if(!last&&this.historyStack.length==0){
last=this.initialState;
}
if(last){
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(_389);
},handleForwardButton:function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.forward();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
},_createState:function(url,args,hash){
return {"url":url,"kwArgs":args,"urlHash":hash};
},_getUrlQuery:function(url){
var _390=url.split("?");
if(_390.length<2){
return null;
}else{
return _390[1];
}
},_loadIframeHistory:function(){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
dojo.io.checkChildrenForFile=function(node){
var _393=false;
var _394=node.getElementsByTagName("input");
dojo.lang.forEach(_394,function(_395){
if(_393){
return;
}
if(_395.getAttribute("type")=="file"){
_393=true;
}
});
return _393;
};
dojo.io.formHasFile=function(_396){
return dojo.io.checkChildrenForFile(_396);
};
dojo.io.updateNode=function(node,_398){
node=dojo.byId(node);
var args=_398;
if(dojo.lang.isString(_398)){
args={url:_398};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
if(dojo["event"]){
try{
dojo.event.browser.clean(node.firstChild);
}
catch(e){
}
}
node.removeChild(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_39f,_3a0,_3a1){
if((!_39f)||(!_39f.tagName)||(!_39f.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_3a1){
_3a1=dojo.io.formFilter;
}
var enc=/utf/i.test(_3a0||"")?encodeURIComponent:dojo.string.encodeAscii;
var _3a3=[];
for(var i=0;i<_39f.elements.length;i++){
var elm=_39f.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_3a1(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_3a3.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_3a3.push(name+"="+enc(elm.value));
}
}else{
_3a3.push(name+"="+enc(elm.value));
}
}
}
var _3a9=_39f.getElementsByTagName("input");
for(var i=0;i<_3a9.length;i++){
var _3aa=_3a9[i];
if(_3aa.type.toLowerCase()=="image"&&_3aa.form==_39f&&_3a1(_3aa)){
var name=enc(_3aa.name);
_3a3.push(name+"="+enc(_3aa.value));
_3a3.push(name+".x=0");
_3a3.push(name+".y=0");
}
}
return _3a3.join("&")+"&";
};
dojo.io.FormBind=function(args){
this.bindArgs={};
if(args&&args.formNode){
this.init(args);
}else{
if(args){
this.init({formNode:args});
}
}
};
dojo.lang.extend(dojo.io.FormBind,{form:null,bindArgs:null,clickedButton:null,init:function(args){
var form=dojo.byId(args.formNode);
if(!form||!form.tagName||form.tagName.toLowerCase()!="form"){
throw new Error("FormBind: Couldn't apply, invalid form");
}else{
if(this.form==form){
return;
}else{
if(this.form){
throw new Error("FormBind: Already applied to a form");
}
}
}
dojo.lang.mixin(this.bindArgs,args);
this.form=form;
this.connect(form,"onsubmit","submit");
for(var i=0;i<form.elements.length;i++){
var node=form.elements[i];
if(node&&node.type&&dojo.lang.inArray(["submit","button"],node.type.toLowerCase())){
this.connect(node,"onclick","click");
}
}
var _3b0=form.getElementsByTagName("input");
for(var i=0;i<_3b0.length;i++){
var _3b1=_3b0[i];
if(_3b1.type.toLowerCase()=="image"&&_3b1.form==form){
this.connect(_3b1,"onclick","click");
}
}
},onSubmit:function(form){
return true;
},submit:function(e){
e.preventDefault();
if(this.onSubmit(this.form)){
dojo.io.bind(dojo.lang.mixin(this.bindArgs,{formFilter:dojo.lang.hitch(this,"formFilter")}));
}
},click:function(e){
var node=e.currentTarget;
if(node.disabled){
return;
}
this.clickedButton=node;
},formFilter:function(node){
var type=(node.type||"").toLowerCase();
var _3b8=false;
if(node.disabled||!node.name){
_3b8=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_3b8=node==this.clickedButton;
}else{
_3b8=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _3b8;
},connect:function(_3b9,_3ba,_3bb){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_3b9,_3ba,this,_3bb);
}else{
var fcn=dojo.lang.hitch(this,_3bb);
_3b9[_3ba]=function(e){
if(!e){
e=window.event;
}
if(!e.currentTarget){
e.currentTarget=e.srcElement;
}
if(!e.preventDefault){
e.preventDefault=function(){
window.event.returnValue=false;
};
}
fcn(e);
};
}
}});
dojo.io.XMLHTTPTransport=new function(){
var _3be=this;
var _3bf={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_3c1,_3c2){
return url+"|"+_3c1+"|"+_3c2.toLowerCase();
}
function addToCache(url,_3c4,_3c5,http){
_3bf[getCacheKey(url,_3c4,_3c5)]=http;
}
function getFromCache(url,_3c8,_3c9){
return _3bf[getCacheKey(url,_3c8,_3c9)];
}
this.clearCache=function(){
_3bf={};
};
function doLoad(_3ca,http,url,_3cd,_3ce){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_3ca.method.toLowerCase()=="head"){
var _3d0=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _3d0;
};
var _3d1=_3d0.split(/[\r\n]+/g);
for(var i=0;i<_3d1.length;i++){
var pair=_3d1[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_3ca.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_3ca.mimetype=="text/json"||_3ca.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_3ca.mimetype=="application/xml")||(_3ca.mimetype=="text/xml")){
ret=http.responseXML;
if(!ret||typeof ret=="string"||!http.getResponseHeader("Content-Type")){
ret=dojo.dom.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
}
if(_3ce){
addToCache(url,_3cd,_3ca.method,http);
}
_3ca[(typeof _3ca.load=="function")?"load":"handle"]("load",ret,http,_3ca);
}else{
var _3d4=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_3ca[(typeof _3ca.error=="function")?"error":"handle"]("error",_3d4,http,_3ca);
}
}
function setHeaders(http,_3d6){
if(_3d6["headers"]){
for(var _3d7 in _3d6["headers"]){
if(_3d7.toLowerCase()=="content-type"&&!_3d6["contentType"]){
_3d6["contentType"]=_3d6["headers"][_3d7];
}else{
http.setRequestHeader(_3d7,_3d6["headers"][_3d7]);
}
}
}
}
this.inFlight=[];
this.inFlightTimer=null;
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
}
};
this.watchInFlight=function(){
var now=null;
if(!dojo.hostenv._blockAsync&&!_3be._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
try{
var tif=this.inFlight[x];
if(!tif||tif.http._aborted||!tif.http.readyState){
this.inFlight.splice(x,1);
continue;
}
if(4==tif.http.readyState){
this.inFlight.splice(x,1);
doLoad(tif.req,tif.http,tif.url,tif.query,tif.useCache);
}else{
if(tif.startTime){
if(!now){
now=(new Date()).getTime();
}
if(tif.startTime+(tif.req.timeoutSeconds*1000)<now){
if(typeof tif.http.abort=="function"){
tif.http.abort();
}
this.inFlight.splice(x,1);
tif.req[(typeof tif.req.timeout=="function")?"timeout":"handle"]("timeout",null,tif.http,tif.req);
}
}
}
}
catch(e){
try{
var _3db=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_3db,tif.http,tif.req);
}
catch(e2){
dojo.debug("XMLHttpTransport error callback failed: "+e2);
}
}
}
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _3dc=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_3dd){
return _3dc&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_3dd["mimetype"].toLowerCase()||""))&&!(_3dd["formNode"]&&dojo.io.formHasFile(_3dd["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_3de){
if(!_3de["url"]){
if(!_3de["formNode"]&&(_3de["backButton"]||_3de["back"]||_3de["changeUrl"]||_3de["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_3de);
return true;
}
}
var url=_3de.url;
var _3e0="";
if(_3de["formNode"]){
var ta=_3de.formNode.getAttribute("action");
if((ta)&&(!_3de["url"])){
url=ta;
}
var tp=_3de.formNode.getAttribute("method");
if((tp)&&(!_3de["method"])){
_3de.method=tp;
}
_3e0+=dojo.io.encodeForm(_3de.formNode,_3de.encoding,_3de["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_3de["file"]){
_3de.method="post";
}
if(!_3de["method"]){
_3de.method="get";
}
if(_3de.method.toLowerCase()=="get"){
_3de.multipart=false;
}else{
if(_3de["file"]){
_3de.multipart=true;
}else{
if(!_3de["multipart"]){
_3de.multipart=false;
}
}
}
if(_3de["backButton"]||_3de["back"]||_3de["changeUrl"]){
dojo.undo.browser.addToHistory(_3de);
}
var _3e3=_3de["content"]||{};
if(_3de.sendTransport){
_3e3["dojo.transport"]="xmlhttp";
}
do{
if(_3de.postContent){
_3e0=_3de.postContent;
break;
}
if(_3e3){
_3e0+=dojo.io.argsFromMap(_3e3,_3de.encoding);
}
if(_3de.method.toLowerCase()=="get"||!_3de.multipart){
break;
}
var t=[];
if(_3e0.length){
var q=_3e0.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_3de.file){
if(dojo.lang.isArray(_3de.file)){
for(var i=0;i<_3de.file.length;++i){
var o=_3de.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_3de.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_3e0=t.join("\r\n");
}
}while(false);
var _3e9=_3de["sync"]?false:true;
var _3ea=_3de["preventCache"]||(this.preventCache==true&&_3de["preventCache"]!=false);
var _3eb=_3de["useCache"]==true||(this.useCache==true&&_3de["useCache"]!=false);
if(!_3ea&&_3eb){
var _3ec=getFromCache(url,_3e0,_3de.method);
if(_3ec){
doLoad(_3de,_3ec,url,_3e0,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_3de);
var _3ee=false;
if(_3e9){
var _3ef=this.inFlight.push({"req":_3de,"http":http,"url":url,"query":_3e0,"useCache":_3eb,"startTime":_3de.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_3be._blockAsync=true;
}
if(_3de.method.toLowerCase()=="post"){
if(!_3de.user){
http.open("POST",url,_3e9);
}else{
http.open("POST",url,_3e9,_3de.user,_3de.password);
}
setHeaders(http,_3de);
http.setRequestHeader("Content-Type",_3de.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_3de.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_3e0);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_3de,{status:404},url,_3e0,_3eb);
}
}else{
var _3f0=url;
if(_3e0!=""){
_3f0+=(_3f0.indexOf("?")>-1?"&":"?")+_3e0;
}
if(_3ea){
_3f0+=(dojo.string.endsWithAny(_3f0,"?","&")?"":(_3f0.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_3de.user){
http.open(_3de.method.toUpperCase(),_3f0,_3e9);
}else{
http.open(_3de.method.toUpperCase(),_3f0,_3e9,_3de.user,_3de.password);
}
setHeaders(http,_3de);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_3de,{status:404},url,_3e0,_3eb);
}
}
if(!_3e9){
doLoad(_3de,http,url,_3e0,_3eb);
_3be._blockAsync=false;
}
_3de.abort=function(){
try{
http._aborted=true;
}
catch(e){
}
return http.abort();
};
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_3f2,days,path,_3f5,_3f6){
var _3f7=-1;
if(typeof days=="number"&&days>=0){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_3f7=d.toGMTString();
}
_3f2=escape(_3f2);
document.cookie=name+"="+_3f2+";"+(_3f7!=-1?" expires="+_3f7+";":"")+(path?"path="+path:"")+(_3f5?"; domain="+_3f5:"")+(_3f6?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _3fb=document.cookie.substring(idx+name.length+1);
var end=_3fb.indexOf(";");
if(end==-1){
end=_3fb.length;
}
_3fb=_3fb.substring(0,end);
_3fb=unescape(_3fb);
return _3fb;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_402,_403,_404){
if(arguments.length==5){
_404=_402;
_402=null;
_403=null;
}
var _405=[],_406,_407="";
if(!_404){
_406=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_406){
_406={};
}
for(var prop in obj){
if(prop==null){
delete _406[prop];
}else{
if(typeof obj[prop]=="string"||typeof obj[prop]=="number"){
_406[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _406){
_405.push(escape(prop)+"="+escape(_406[prop]));
}
_407=_405.join("&");
}
dojo.io.cookie.setCookie(name,_407,days,path,_402,_403);
};
dojo.io.cookie.getObjectCookie=function(name){
var _40a=null,_40b=dojo.io.cookie.getCookie(name);
if(_40b){
_40a={};
var _40c=_40b.split("&");
for(var i=0;i<_40c.length;i++){
var pair=_40c[i].split("=");
var _40f=pair[1];
if(isNaN(_40f)){
_40f=unescape(pair[1]);
}
_40a[unescape(pair[0])]=_40f;
}
}
return _40a;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _410=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_410=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.provide("dojo.io.*");
dojo.provide("dojo.io");
dojo.deprecated("dojo.io","replaced by dojo.io.*","0.5");

