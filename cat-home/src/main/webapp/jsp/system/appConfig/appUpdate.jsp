<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appList').addClass('active');
		});
		
		$(document).delegate('#updateSubmit', 'click', function(e){
			var name = $("#commandName").val();
			var title = $("#commandTitle").val();
			var domain = $("#commandDomain").val();
			var id = $("#commandId").val();
			
			if(name == undefined || name == ""){
				if($("#errorMessage").length == 0){
					$("#commandName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			$.ajax({
				async: false,
				type: "get",
				dataType: "json",
				url: "/cat/s/config?op=appNameCheck&name="+name,
				success : function(response, textStatus) {
					if(response['isNameUnique']){
						if(title==undefined){
							title = "";
						}
						if(domain==undefined){
							domain="";
						}
						if(id==undefined){
							id="";
						}
						
						window.location.href = "/cat/s/config?op=appSubmit&name="+name+"&title="+title+"&domain="+domain+"&id="+id;
					}else{
						alert("该名称已存在，请修改名称！");
					}
				}
			});
		})
	</script>
	
	<table class="table table-striped table-condensed table-bordered table-hover">
		<tr>
			<td>名称</td><td><input name="name" value="${model.updateCommand.name}" id="commandName"/><br/>
		</td>
		<tr>
		<tr>
			<td>项目</td><td><input name="domain" value="${model.updateCommand.domain}" id="commandDomain" /><span class="text-danger">（* 支持数字、字符）</span><br/>
</td>
</tr>
		<tr><td>标题</td><td><input name="title" value="${model.updateCommand.title}" id="commandTitle" /><span class="text-danger">（* 支持数字、字符）</span><br/>
			</td>
		</tr>
		<c:if test="${not empty payload.id}">
			<input name="id" value="${payload.id}" id="commandId" style="display:none"/>
		</c:if>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary btn-sm" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:config>
