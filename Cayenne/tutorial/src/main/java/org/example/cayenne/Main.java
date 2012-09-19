package org.example.cayenne;

import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.query.SelectQuery;
import org.example.cayenne.persistent.Hockey;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ObjectContext context = DataContext.createDataContext();

		SelectQuery select1 = new SelectQuery(Hockey.class);
		List<Hockey> players = context.performQuery(select1);
		System.out.println("List of all players:");
		for(Hockey h : players) {
			System.out.println(h.getName()+" - "+h.getPosition()+" for "+h.getTeam());
		}
		// add new player
		System.out.println();
		System.out.println("Adding a new player...");
		Hockey player=context.newObject(Hockey.class);
		player.setName("Rookie");
		player.setNumber(99);
		player.setPosition("Reserve");
		player.setTeam("Bruins");
		context.commitChanges();
		// print the new list
		System.out.println();
		System.out.println("List of all players:");
		select1 = new SelectQuery(Hockey.class);
		players = context.performQuery(select1);
		for(Hockey h : players) {
			System.out.println(h.getName()+" - "+h.getPosition()+" for "+h.getTeam());
		}
		// delete new player
		System.out.println();
		System.out.println("Removing the new player...");
		context.deleteObject(player);
		context.commitChanges();
	}

}
