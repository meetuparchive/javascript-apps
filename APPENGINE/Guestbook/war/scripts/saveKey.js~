//Sets a cookie
function setCookie(c_name,value,expiredays)
{
	var exdate=new Date();
	exdate.setDate(exdate.getDate()+expiredays);
	document.cookie=c_name+ "=" +escape(value)+
	((expiredays==null) ? "" : ";expires="+exdate.toUTCString());
}

//Returns a cookie given by c_name
function getCookie(c_name)
{
	if (document.cookie.length>0)
	{
 		 c_start=document.cookie.indexOf(c_name + "=");
 		 if (c_start!=-1)
   		 {
    			 c_start=c_start + c_name.length+1;
    			 c_end=document.cookie.indexOf(";",c_start);
    			 if (c_end==-1) c_end=document.cookie.length;
    				return unescape(document.cookie.substring(c_start,c_end));
   	 	 }
  	}
	return "";
}

//Uses a given boolean to check if cookie should be added, or deleted.
function checkAddCookie(apiKey,bool) {
	if(bool) {
		//remember
		setCookie('myApiKey',apiKey,365);
	}
	else {
		//Dont remember - if same as stored value, delete cookie
		if (getAPIcookie() == apiKey) {
			setCookie('myApiKey',apiKey,-1);
		}
	}
}

function getAPIcookie() {
	return getCookie('myApiKey');
}
