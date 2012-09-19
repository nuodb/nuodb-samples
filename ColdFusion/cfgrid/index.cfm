<cfwindow initshow="true" center="true"
            width="580" height="340" title="Hockey">

<cfform>
    <cfgrid name="players"
            format="html"
            pagesize="10"
            striperows="yes"
			selectmode="edit"
			delete="yes" 
			insert="yes"
			onchange="cfc:hockey.editPlayer({cfgridaction}, {cfgridrow},{cfgridchanged})"
            bind="cfc:hockey.getHockeyPlayers({cfgridpage},
                                        {cfgridpagesize},
                                        {cfgridsortcolumn},
                                        {cfgridsortdirection})">
        <cfgridcolumn name="number" header="Number" width="60"/>
        <cfgridcolumn name="position" header="Position" width="100"/>
        <cfgridcolumn name="name" header="Name" width="200"/>
        <cfgridcolumn name="team" header="Team" width="200"/>
    </cfgrid>
</cfform>


</cfwindow>

