/**
 * BuildStatConfigs class definition
 * Needs following things when included :
 * - BUILD_STAT_CONTAINER_ID_PREFIX global constant
 * - chartList.js file inclusion
 */
var BuildStatConfigs = Class.create();
BuildStatConfigs.prototype = {
	initialize: function(){
		this.ids = new Array();
	},
	add: function(buildStatConfig){
		this.createChartElement(buildStatConfig);
		this[buildStatConfig.id] = buildStatConfig;
		this.ids[this.size()] = buildStatConfig.id;
	},
	update: function(bsId, buildStatConfig){
		this.updateChartElement(bsId, buildStatConfig);
		this[buildStatConfig.id] = buildStatConfig;
		
		// Is id is updated, remove old id reference
		if(bsId != buildStatConfig.id){
			this[bsId] = null;
			this.ids = this.ids.without(bsId);
		}
	},
	// renamed from delete to deleteChart since in chrome_linux, delete is a reserved keyword
	deleteChart: function(buildStatId){
		this.deleteChartElement(buildStatId);
		this[buildStatId] = null;
		this.ids = this.ids.without(buildStatId);
	},
	deleteChartElement: function(buildStatId){
		var buildStatContainerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatId;
		var previousBuildStatContainer = this.getPreviousBuildStatConfigContainer(buildStatContainerId);
		$(buildStatContainerId).update("");
		$(buildStatContainerId).id="deletedBuildStatConfig";
		if(previousBuildStatContainer != null){
			this.updateButtonsFor(this.retrieveBuildStatIdFromContainerId(previousBuildStatContainer.id));
		}
	},
	getBuildStat: function(buildStatId){
		return this[buildStatId];
	},
	size: function(){
		return this.ids.length;
	},
	getHTMLWithoutContainerFromBuildStatConfig: function(buildStatConfiguration){
		var currentContext = createTemplateContext(buildStatConfiguration);
		
		var imageTemplateStr = '';
		imageTemplateStr += '<img style="display:inline; float:left; margin-bottom: 10px; margin-right: 10px;" id="img_#{id}" \n';
		imageTemplateStr += 'src="#{rootURL}/plugin/global-build-stats/showChart?buildStatId=#{id}&time=#{currentTime}" />\n';
		imageTemplateStr += '<div id="map_#{id}_container"></div><br/>\n';
		var imageTemplate = new Template(imageTemplateStr);
		var image = imageTemplate.evaluate(currentContext);
		
		currentContext = jsonConcat(currentContext, { buildStatImage: image});
		
		var buildStatConfigWithoutContainerTemplate = new Template(getTemplateContent('buildStatConfigWithoutContainerTemplate'));
		var buildStatConfigWithoutContainerHTML = buildStatConfigWithoutContainerTemplate.evaluate(currentContext);
		
		return buildStatConfigWithoutContainerHTML;
	},
	isBuildStatConfigContainer: function(htmlElement){
		return htmlElement != null && htmlElement.id != null && htmlElement.id.startsWith(BUILD_STAT_CONTAINER_ID_PREFIX);
	},
	getPreviousBuildStatConfigContainer: function(currentBuildStatConfigContainerId){
		var container = $(currentBuildStatConfigContainerId).previous();
		while(container != null && !this.isBuildStatConfigContainer(container)){
			container = container.previous();
		}
		return container;
	},
	getNextBuildStatConfigContainer: function(currentBuildStatConfigContainerId){
		var container = $(currentBuildStatConfigContainerId).next();
		while(container != null && !this.isBuildStatConfigContainer(container)){
			container = container.next();
		}
		return container;
	},
	updateButtonsFor: function(buildStatConfigId){
		var containerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfigId;
		var container = $(containerId);
		
		if($('moveUp_'+buildStatConfigId) != null){
			if(this.getPreviousBuildStatConfigContainer(containerId) != null){
				$('moveUp_'+buildStatConfigId).show();
			} else {
				$('moveUp_'+buildStatConfigId).hide();
			}
		}
		if($('moveDown_'+buildStatConfigId) != null){
			if(this.getNextBuildStatConfigContainer(containerId) != null){
				$('moveDown_'+buildStatConfigId).show();
			} else {
				$('moveDown_'+buildStatConfigId).hide();
			}
		}
	},
	swapBuildStatConfigs: function(containerId1, containerId2){
		var buildStatConf1 = this.getBuildStatConfigFromContainerId(containerId1);
		var buildStatConf2 = this.getBuildStatConfigFromContainerId(containerId2);
	
		var buildStatConf1Container = $(BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf1.id);
		var buildStatConf2Container = $(BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf2.id);
		
		var replacedHTML = buildStatConf1Container.innerHTML;
		buildStatConf1Container.innerHTML = buildStatConf2Container.innerHTML;
		buildStatConf2Container.innerHTML = replacedHTML;
		
		buildStatConf1Container.id = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf2.id;
		buildStatConf2Container.id = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf1.id;
		
		this.updateButtonsFor(buildStatConf1.id);
		this.updateButtonsFor(buildStatConf2.id);
	},
	retrieveBuildStatIdFromContainerId: function(containerId){
		var extractingRegex = new RegExp("^"+BUILD_STAT_CONTAINER_ID_PREFIX+"(.*)$", "g");
		var buildStatId = null;
		if(extractingRegex.test(containerId)){
			extractingRegex.exec(containerId);
			buildStatId = RegExp.$1;
		}
		return buildStatId;
	},
	getBuildStatConfigFromContainerId: function(containerId){
		var buildStatConfigId = this.retrieveBuildStatIdFromContainerId(containerId);
		var buildStatConfig = null;
		if(buildStatConfigId != null){
			buildStatConfig = this.getBuildStat(buildStatConfigId);
		}
		return buildStatConfig;
	},
	fillDivWithChart: function(divId, buildStatConfig, updateButtonsCallback){
		ajaxCall('link', rootURL+'/plugin/global-build-stats/createChartMap?buildStatId='+buildStatConfig.id, function(ret){
			var content = BUILD_STAT_CONFIGS.getHTMLWithoutContainerFromBuildStatConfig(buildStatConfig);
			$(divId).update(content);
			$('map_'+buildStatConfig.id+'_container').update(ret.responseText);
			var mapId = "map_"+buildStatConfig.id;
			$('map_'+buildStatConfig.id+'_container').firstChild.setAttribute("name", mapId);
			$('img_'+buildStatConfig.id).setAttribute("usemap", "#" + mapId);
			
			updateButtonsCallback.call(null);
		}, true);
	},
	updateChartElement: function(bsId, buildStatConfig){
		$(BUILD_STAT_CONTAINER_ID_PREFIX+bsId).id = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfig.id;
		this.fillDivWithChart(BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfig.id, buildStatConfig, function(){
			BUILD_STAT_CONFIGS.updateButtonsFor(buildStatConfig.id);
		});
	},
	createChartElement: function(buildStatConfig){
		if(this.size() == 0){
			$('buildStatConfigsContainer').update("");
		}
		
		var newBuildStatContainerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfig.id;
		
		// This stuff could be simpler with Prototype 1.6...
		var newBuildStatConf = document.createElement("div");
		newBuildStatConf.setAttribute("style", "clear:left");
		newBuildStatConf.setAttribute("id", newBuildStatContainerId);
		
		$('buildStatConfigsContainer').appendChild(newBuildStatConf);
		this.fillDivWithChart(newBuildStatContainerId, buildStatConfig, function(){
			BUILD_STAT_CONFIGS.updateButtonsFor(buildStatConfig.id);
			var previousBuildStatContainer = BUILD_STAT_CONFIGS.getPreviousBuildStatConfigContainer(newBuildStatContainerId);
			if(previousBuildStatContainer != null){
				BUILD_STAT_CONFIGS.updateButtonsFor(BUILD_STAT_CONFIGS.retrieveBuildStatIdFromContainerId(previousBuildStatContainer.id));
			}
		});
	},
	moveBuildStat: function(buildStatId, moveType){
		var moveUrl = "";
		if(moveType.toLowerCase() == "up"){
			moveUrl = rootURL+'/plugin/global-build-stats/moveUpConf?buildStatId='+buildStatId;
		} else if(moveType.toLowerCase() == "down"){
			moveUrl = rootURL+'/plugin/global-build-stats/moveDownConf?buildStatId='+buildStatId;
		}
		
		ajaxCall('link', moveUrl, function(transport) {
		  	var currentContainerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatId;
			var currentChartContainer = $(currentContainerId);
			var otherChartContainer = null;
			if(moveType.toLowerCase() == "up"){
				otherChartContainer = BUILD_STAT_CONFIGS.getPreviousBuildStatConfigContainer(currentContainerId);
			} else if(moveType.toLowerCase() == "down"){
				otherChartContainer = BUILD_STAT_CONFIGS.getNextBuildStatConfigContainer(currentContainerId);
			}
			
			BUILD_STAT_CONFIGS.swapBuildStatConfigs(currentChartContainer.id, otherChartContainer.id);
			
			window.location.href = "#"+BUILD_STAT_CONTAINER_ID_PREFIX+buildStatId;
		});
	}
};
