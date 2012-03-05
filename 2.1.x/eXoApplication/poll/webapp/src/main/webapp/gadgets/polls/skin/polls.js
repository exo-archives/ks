var displayData = "none";
var displayView = "node";

String.prototype.trim = function () {
return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

function createRequestUrl(){
	var prefs = new gadgets.Prefs();
	var pollId = prefs.getString("pollId");
	var restURL = "/portal/rest";
	if (top.location.href.indexOf("ksdemo") != -1) restURL = "/ksdemo/rest-ksdemo" 
	var url = restURL + "/private/ks/poll/viewpoll/"; 
	if(pollId) {
		pollId = new String(pollId);
		url = url + pollId.trim() ;
	} else url = url + "pollid" ;
	return url;
}

// render data.
function render(data){
	var clickEditPoll = document.getElementById("EditPollIcon");
	var tpPoll = document.getElementById("TopicPollContent");
	var viewPoll = document.getElementById("ViewPollForm");
	var comment = document.getElementById("comment");
	var editPoll = document.getElementById("EditPoll");
	if(data) {
		if(data.isAdmin === "true") {
			clickEditPoll.className = "IconRight EditPollIcon";
			clickEditPoll.onclick = editUserPrefs;
		} else {
			clickEditPoll.className = "FloatRight";
			clickEditPoll.onclick = function(){};
		}
		if(data.id === "Empty") {
			editPoll.style.display = "block";
			buildMenu(data);
		} else if(data.id === "DoNotPermission") {
			comment.style.display = "block";
			tpPoll.style.display = "none";
			viewPoll.style.display = "none";
			editPoll.style.display = "none";
		} else {
			editPoll.style.display = "none";
			comment.style.display = "none";
			if(data.showVote) {
				tpPoll.style.display = "none";
				viewPoll.style.display = "block";
				innerData("span", "question", data.question);
				innerTableData(data);
			} else {
				tpPoll.style.display = "block";
				viewPoll.style.display = "none";
				innerData("span", "question", data.question);
				innerRadioData("options", data) ;
				displayView = "node";
			}
		}
	}
	adjustHeight();
}

function buildMenu(data) {
	var ids = data.pollId;
	var pollNames = data.pollName;
	var pollId = gadgets.Prefs().getString("pollId");
	pollId = String(pollId);
	var cmSeclectPoll = gadgets.Prefs().getMsg("cmSelectPoll");
	var selectPoll = gadgets.Prefs().getMsg("selectPoll");
	
	var str =
		'<form>'+
			'<div style="padding: 8px 0px;">'+
				'<table class="UIFormGrid" style="width:100%">'+
					'<tbody>'+
						'<tr>'+
							'<td colspan="3"><div>' + cmSeclectPoll + '</div></td>'+
						'</tr>'+
						'<tr>'+
							'<td class="FieldLabel"><div style="text-align:right;padding-right:8px">' + selectPoll + '</div></td>'+
							'<td colspan="2" class="FieldComponent">'+
								'<select id="selectbox" name="pollSelect" class="selectbox">';
							for(var i = 0; i < ids.length; ++ i) {
								if(pollId === ids[i]){
									ids[i] = ids[i] + '" selected="selected';
								}
								str = str + 
								'<option value="'+ids[i]+'" class="SelectBox">'+pollNames[i]+'</option>';
							}
							str = str + 
								'</select>'+
							'</td>'+
						'</tr>'+
						'<tr>'+
							'<td>'+
								'<div style="padding:10px 3px;">'+
									'<input value="Save" onclick="saveUserPrefs()" type="button" style="padding: 0px 5px;float: right;"/>'+
									'<div style="clear:right;"><span></span></div>'+
								'</div>'+
							'</td>'+
							'<td colspan="2">'+
								'<div style="padding:10px 3px;">'+
									'<input value="Cancel" onclick="cancelUserPrefs()" type="button"/>'+
								'</div>'+
							'</td>'+
						'</tr>'+
					'</tbody>'+
				'</table>'+
			'</div>'+
		'</form>';
	
	document.getElementById("SelectContent").innerHTML = str;
}

function innerData(type, clasElm, vl) {
	var elms = document.getElementsByTagName(type);
	for(var i = 0; i < elms.length; ++ i) {
		if(elms[i].className === clasElm) elms[i].innerHTML = vl;
	}
}

function innerRadioData(idElm, data) {
	var options = data.option;
	var str = "<form>";
	for(var i = 0; i < options.length; ++ i) {
		str = str + '<div><input class="radio" type="radio" name="vote" value="' + options[i] + '"> <span>' + options[i] + '</span></div>';
	}
	str = str + "</form>";
	document.getElementById(idElm).innerHTML = str;
}


function innerTableData(data) {
	var options = data.option;
	var infoVote = data.infoVote;
	var colors = ["blue", "DarkGoldenRod", "green", "yellow", "BlueViolet", "orange", "darkBlue", "IndianRed", "DarkCyan", "lawnGreen"];
	var classCss = "OddRow" ;
	var str = "";
	var percen = "10%";
	var voteNumber = 2;
	for(var i = 0; i < options.length; ++ i) {
		var number = infoVote[i].split(":") ;
		percen = number[0];
		str = str +
		'<tr class="' + classCss + '">' +
			'<td class="text">' + options[i] + '</span></td>' +
			'<td>' +
				'<table class="Percen">' +
					'<tbody>' +
						'<tr>' +
							'<td class="BackgroudColor">' +
								'<div style="background-color:' + colors[i] + '; width: ' + percen + '%;" class="Chart"><span></span></div>' +
							'</td>' +
							'<td class="Percentage">' +
								'<div>' + percen +'%</div>' +
							'</td>' +
						'</tr>' +
					'</tbody>' +
				'</table>' +
			'</td>' +
			'<td class="Number">' + number[1] + '</td>' +
		'</tr>';
		if(i%2 == 0) classCss = "EvenRow" ;
		else classCss = "OddRow";
	}
	document.getElementById("dataTable").innerHTML = str;
	document.getElementById("sunVote").innerHTML = infoVote[(infoVote.length-1)];
}
// get data
function getData(){					 
	var url = createRequestUrl();		
	ajaxAsyncGetRequest(url,render);
}


// vote 
function voteNow() {
	var radios = document.getElementsByTagName('input');
	if(radios) {
		for(var i = 0; i < radios.length; ++ i) {
			if(radios[i].checked){
				var url = createRequestUrl() + "/" + i;
				url = String(url).replace("viewpoll", "votepoll");
				ajaxAsyncGetRequest(url,render);
				break;
			}
		}
	}
}

// save pollId
function editUserPrefs() {
	document.getElementById("TopicPollContent").style.display = "none";
	document.getElementById("ViewPollForm").style.display = "none";
	document.getElementById("EditPoll").style.display = "block";
	var url = createRequestUrl() + "_edit";
	ajaxAsyncGetRequest(url,render);
	innerData("span", "question", "");
}

function saveUserPrefs() {
	var input = document.getElementById("selectbox");
	if(input) {
		var pollId = input.value;
		if(pollId) {
			var prefs = new gadgets.Prefs();
			prefs.set("pollId", pollId);
			document.getElementById("SelectContent").innerHTML = "";
			getData();
		}
	}
}

function cancelUserPrefs() {
	document.getElementById("SelectContent").innerHTML = "";
	getData();
}

// load hander
function onLoadHander(){
	setInterval(getData,5000);
}
function ajaxAsyncGetRequest(url, render) {
	var request =	createHttpRequest() ;
	request.open('GET', url, true) ;
	request.setRequestHeader("Cache-Control", "max-age=86400") ;
	request.send(null) ;
	request.onreadystatechange = function(){
		var data = request.responseText;
		render(gadgets.json.parse(data));
	}					
}

function adjustHeight(){
	var gadgetNode = document.getElementById("UITopicPoll").parentNode;
	var height = gadgetNode.offsetHeight + 10;
	gadgets.window.adjustHeight(height);
}

function createHttpRequest() {
	var xmlhttp;
	try {
	// Mozilla / Safari / IE7
	xmlhttp = new XMLHttpRequest();
	} catch (e) {
	// IE
		var XMLHTTP_IDS = new Array('MSXML2.XMLHTTP.5.0',
		 'MSXML2.XMLHTTP.4.0',
		 'MSXML2.XMLHTTP.3.0',
		 'MSXML2.XMLHTTP',
		 'Microsoft.XMLHTTP' );
		var success = false;
		for (var i=0;i < XMLHTTP_IDS.length && !success; i++) {
			try {
				xmlhttp = new ActiveXObject(XMLHTTP_IDS[i]);
				success = true;
			} catch (e) {}
		}
		if (!success) {
			throw new Error('Unable to create XMLHttpRequest.');
		}
	}
	return xmlhttp;
}
gadgets.util.registerOnLoadHandler(getData);