function createTemplateContext(buildStatConfiguration){
	if(buildStatConfiguration==null || buildStatConfiguration.length == 0){
		// Creating context for creation
		var currentContext = {
			rootURL: rootURL, 
			formAction: "addBuildStatConfiguration", 
			buildStatId: getBuildStatConfigId(null),
			currentTime: new Date().getTime(),
			BUILD_STAT_CONTAINER_ID_PREFIX: BUILD_STAT_CONTAINER_ID_PREFIX,
			CDATAEndAndScript: "]]"+">"+"</"+"script"+">"
		};
	} else {
		// Creating context for update
		var currentContext = jsonConcat(buildStatConfiguration, {
			jobFilter: buildStatConfiguration.buildFilters.jobFilter,
			nodeFilter: buildStatConfiguration.buildFilters.nodeFilter,
			launcherFilter: buildStatConfiguration.buildFilters.launcherFilter,
			rootURL: rootURL, 
			formAction: "updateBuildStatConfiguration", 
			buildStatId: getBuildStatConfigId(buildStatConfiguration.id),
			currentTime: new Date().getTime(),
			BUILD_STAT_CONTAINER_ID_PREFIX: BUILD_STAT_CONTAINER_ID_PREFIX,
			CDATAEndAndScript: "]]"+">"+"</"+"script"+">"
		});
	}
	
	return currentContext;
}

function getBuildStatConfigId(buildStatConfigId){
	if(buildStatConfigId == null){
		return "new";
	} else {
		return buildStatConfigId;
	}
}

function jsonConcat(o1, o2) {
	for (var key in o2) {
		o1[key] = o2[key];
	}
	return o1;
}

// For some unknown reasons, on firefox, some #{XXX} template variables are replaced by #%7BXXX%7D :(
function getTemplateContent(templateId){
	var content = document.getElementById(templateId).innerHTML;
	content = content.replace(new RegExp("%7B", "g"), "{");
	content = content.replace(new RegExp("%7D", "g"), "}");
	return content;
}

function ajaxCall(callType, param, successCallback){
	ajaxCall(callType, param, successCallback, false);
}


function createAjaxCallParams(successCallback) {
  return {
    onSuccess: function(ret) {
      successCallback.call(null, ret);
    },/* For unknown reasons, an exception is thrown after the onSuccess process .. :(
		onException: function(transport, ex) {
			alert('exception : '+ex);
		    throw ex;
		},*/
    onFailure: function(transport) {
      dialog.alert('failure : '+ JSON.stringify(transport));
    }
  };
}

function ajaxCall(callType, param, successCallback, skipLoading){

	var ajaxCallParams = createAjaxCallParams(successCallback);
	
	if(callType == 'form'){
		const form = document.getElementById(param);
		ajaxRequest(form.action, ajaxCallParams, new URLSearchParams(new FormData(form)).toString());
	} else {
		ajaxRequest(param, ajaxCallParams);
	}
}	

function deleteBuildStat(buildStatId){
	var deletionConfirmationMessage = document.getElementById('deletionConfirmationMessage').innerHTML;
	dialog.confirm(deletionConfirmationMessage, {type: 'destructive'}).then(() => {
		ajaxCall('link', 'deleteConfiguration?buildStatId='+buildStatId, function(transport) {
		  	BUILD_STAT_CONFIGS.deleteChart(buildStatId);
		});
	});
}

function ajaxRequest(url, ajaxCallParams, body){
	fetch(url, {
		method: "post",
		headers: crumb.wrap({
			"Content-Type": "application/x-www-form-urlencoded",
		}),
		body: body,
	})
	.then(response => {
		if (response.ok) {
			response.text()
			.then(responseText => {
				ajaxCallParams.onSuccess({responseText: responseText});
			})
		} else {
			ajaxCallParams.onFailure(response);
		}
	});
}


function createElementFromHtml(html) {
  const template = document.createElement("template");
  template.innerHTML = html.trim();
  return template.content.firstElementChild;
}


function evaluateTemplate(content, context){
	return content.replace(
		/#\{(.*?)\}/g,
		function(match, p1, offset, string){
			if (p1 in context) {
				if (context.unsanitized?.includes(p1)){
					return context[p1];
				} else {
					return escapeHTML(context[p1]);
				}
			} else {
				return '';
			}
		}
	);
}

function escapeHTML(str){
	return str.toString().replace(/[&<>'"]/g,
		tag => ({
			'&': '&amp;',
			'<': '&lt;',
			'>': '&gt;',
			"'": '&#x27;',
			'"': '&quot;',
		}[tag])
	);
}

function updateRetentionStrategies(btn){
  ajaxCall('form', 'retentionStrategiesForm', function(ret){
    notificationBar.show(btn.dataset.message, notificationBar.SUCCESS);
  }, true);
}

Behaviour.specify("#updateRetentionStrategies", "updateRetentionStrategies", 0, function(btn) {
  btn.onclick = function() {
    updateRetentionStrategies(btn);
  }
});