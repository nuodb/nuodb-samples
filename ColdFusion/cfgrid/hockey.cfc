<cfcomponent output="false">

    <cfset THIS.dsn="nuodb">

    <!--- Get artists --->
    <cffunction name="getHockeyPlayers" access="remote" returntype="struct">
        <cfargument name="page" type="numeric" required="yes">
        <cfargument name="pageSize" type="numeric" required="yes">
        <cfargument name="gridsortcolumn" type="string" required="no" default="">
        <cfargument name="gridsortdir" type="string" required="no" default="">

        <!--- Local variables --->
        <cfset var players="">

        <!--- Get data --->
        <cfquery name="players" datasource="#THIS.dsn#">
        SELECT *
        FROM hockey
        <cfif ARGUMENTS.gridsortcolumn NEQ ""
            and ARGUMENTS.gridsortdir NEQ "">
            ORDER BY #ARGUMENTS.gridsortcolumn# #ARGUMENTS.gridsortdir#
        </cfif>
        </cfquery>

        <!--- And return it as a grid structure --->
        <cfreturn QueryConvertForGrid(players,
                            ARGUMENTS.page,
                            ARGUMENTS.pageSize)>
    </cffunction>

    <cffunction name="editPlayer" access="remote">
		<cfargument name="gridaction" type="string" required="yes">
		<cfargument name="gridrow" type="struct" required="yes">
		<cfargument name="gridchanged" type="struct" required="yes">

		<CFSWITCH expression="#Arguments.gridaction#">
   
		    <!--- Process Update--->
		    <cfcase value="U">
		        <!---Get column name and value--->
				<cfset colname=StructKeyList(Arguments.gridchanged)>
				<cfset value=StructFind( Arguments.gridchanged, colname )>
          
				<!---Perform actual update--->
            	<cfquery datasource="#THIS.dsn#">
				UPDATE hockey
				SET #colname# = <cfqueryparam value="#value#" />
				WHERE id = <cfqueryparam cfsqltype="cf_sql_integer" value="#Arguments.gridrow.id#" />
				</cfquery>
   
			</cfcase>

			<!---PROCESS DELETES--->
			<cfcase value="D">
				<!--- Perform Actual Delete--->
				<cfquery datasource="#THIS.dsn#">
         		DELETE FROM hockey
            	where id = <cfqueryparam cfsqltype="cf_sql_integer" value="#Arguments.gridrow.id#" />
            	</cfquery>
           
            </cfcase>

			<!---PROCESS INSERTS--->
			<cfcase value="I">
				<!--- Perform Actual Delete--->
				<cfquery datasource="#THIS.dsn#">
         		INSERT into hockey
				(
				number
				, name
				, position
				, team
				)
				VALUES
				(
				<cfqueryparam cfsqltype="cf_sql_integer" value="#Arguments.gridrow.number#" />
				, <cfqueryparam cfsqltype="cf_sql_varchar" value="#Arguments.gridrow.name#" />
				, <cfqueryparam cfsqltype="cf_sql_varchar" value="#Arguments.gridrow.position#" />
				, <cfqueryparam cfsqltype="cf_sql_varchar" value="#Arguments.gridrow.team#" />
				)
            	</cfquery>
           
            </cfcase>
		</CFSWITCH>
			
    </cffunction>

</cfcomponent>