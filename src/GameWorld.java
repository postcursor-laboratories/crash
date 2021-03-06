import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

public class GameWorld {

	static final float LAT_MOVE_FORCE = 10;
	static final float GRAV = -32;
	static final float scale = 30;
	static final int BEAM_HW = 400;
	static final int BASE_H = 300;
	
	private HashMap<Body,Integer> bodyIds;
	private HashMap<Integer,Body> bodies;
	HashSet<Body> deadBodies;
	private int nextBodyId;
	
	private ArrayList<Body> goneBodies;
	private ArrayList<Body> newBodies;
	ArrayList<Body> newDeadBodies;
	
	World _world;
	Player _currPlayer;

	public GameWorld(){
		bodyIds = new HashMap<>();
		bodies = new HashMap<>();
		deadBodies = new HashSet<>();
		nextBodyId = 100;
		goneBodies = new ArrayList<>();
		newBodies = new ArrayList<>();
		newDeadBodies = new ArrayList<>();
		
		_world = new World(new Vec2(0, GRAV), false);
	}
	
	void init(){

		{
			BodyDef beamDef = new BodyDef();
			beamDef.type = BodyType.KINEMATIC;
			beamDef.position = new Vec2(8, 0);
			Body beam = _world.createBody(beamDef);
	
			PolygonShape beamShape = new PolygonShape();
			beamShape.setAsBox(8, 0.3f);
			beam.createFixture(beamShape, 0.1f);
			beam.m_fixtureList.m_friction = 1f;
			
			addBody(beam);
		}
		
		{
			BodyDef beamDef = new BodyDef();
			beamDef.type = BodyType.KINEMATIC;
			beamDef.position = new Vec2(-8, 0);
			Body beam = _world.createBody(beamDef);
	
			PolygonShape beamShape = new PolygonShape();
			beamShape.setAsBox(8, 0.3f);
			beam.createFixture(beamShape, 0.1f);
			beam.m_fixtureList.m_friction = 1f;
			
			addBody(beam);
		}
		
		{
			BodyDef playerDef = new BodyDef();
			playerDef.type = BodyType.DYNAMIC;
			playerDef.position = new Vec2(0, 3.5f);
			Body player = _world.createBody(playerDef);
			
			CircleShape playerShape = new CircleShape();
			playerShape.m_radius = 2;
			
			player.createFixture(playerShape, 0.1f);
			player.m_fixtureList.m_friction = 1f;
			
			addBody(player);
		}

		addWall(-30, 0, 0.1f, 16f, 0.5f);
		addWall(30, 0, 0.1f, 16f, 0.5f);
		addWall(0, -15.9f, 30f, 0.1f, 0.5f);
		addWall(0, 15.9f, 30f, 0.1f, 0.5f);
	}
	
	void addWall(float x, float y, float w, float h, float friction) {
		BodyDef beamDef = new BodyDef();
		beamDef.type = BodyType.STATIC;
		beamDef.position = new Vec2(x, y);
		Body beam = _world.createBody(beamDef);

		PolygonShape beamShape = new PolygonShape();
		beamShape.setAsBox(w, h);
		beam.createFixture(beamShape, .1f);
		beam.m_fixtureList.m_friction = friction;
		
		addBody(beam);
	}
	
	void addDynamicBox(float x, float y, float w, float h, float friction, float density) {
		BodyDef boxDef = new BodyDef();
		boxDef.type = BodyType.DYNAMIC;
		boxDef.position = new Vec2(x, y);
		Body box = _world.createBody(boxDef);

		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(w, h);
		box.createFixture(boxShape, density);
		box.m_fixtureList.m_friction = friction;
		
		addBody(box);
	}
	
	//For server
	void addBody(Body b){
		addBody(nextBodyId++, b);
	}
	
	//For client, receiving a given ID
	void addBody(int bodyId, Body b){
		bodyIds.put(b, bodyId);
		bodies.put(bodyId, b);
		newBodies.add(b);
	}
	
	void removeBody(Body b){
		if(bodies.remove(b) == null){
			throw new RuntimeException("Tried to clear body "+b+", wasn't present.");
		}
		goneBodies.add(b);
	}
	
	void tick(){
		
		//Cleanup
		for(Body b : goneBodies){
			bodyIds.remove(b);
		}
		newBodies.clear();
		goneBodies.clear();
		newDeadBodies.clear();
		
		//Execution
		bodies.get(100).m_angularVelocity +=
				+ (float)((Math.random()-0.49)/50);
		bodies.get(100).m_angularVelocity *= 0.99;
		bodies.get(101).m_angularVelocity +=
				+ (float)((Math.random()-0.51)/50);
		bodies.get(101).m_angularVelocity *= 0.99;
		
		synchronized(_world){
			_world.step(0.03f, 6, 3);
		}
	}
	
	void sendInit(DataOutputStream cli, Player player) throws IOException{
		cli.writeInt(bodies.size());
		for(Body b : bodies.values()){
			//New body information; what type?
			cli.writeInt(bodyIds.get(b));
			writeNewBody(cli, b);
		}
		cli.writeInt(deadBodies.size());
		for (Body b : deadBodies) {
			cli.writeInt(bodyIds.get(b));
		}
		cli.writeInt(bodyIds.get(player.b));
		player.initialized = true;
	}
	
	void sendWorld(DataOutputStream cli) throws IOException{
		cli.writeInt(1337); //Start of packet; used as a signal of data availability, too.
		
		cli.writeInt(newBodies.size());
		for(Body b : newBodies){
			//New body information; what type?
			cli.writeInt(bodyIds.get(b));
			writeNewBody(cli, b);
		}
		
		cli.writeInt(goneBodies.size());
		for(Body b : goneBodies){
			cli.writeInt(bodyIds.get(b));
		}

		cli.writeInt(newDeadBodies.size());
		for(Body b : newDeadBodies){
			cli.writeInt(bodyIds.get(b));
		}
		
		for(Body b : bodies.values()){
			cli.writeInt(bodyIds.get(b));
			writeVec(cli,b.getPosition());
			cli.writeFloat(b.getAngle());
			writeVec(cli,b.getLinearVelocity());
			cli.writeFloat(b.getAngularVelocity());
		}
	}
	
	void writeNewBody(DataOutputStream cli, Body b) throws IOException{
		cli.writeInt(b.m_type.ordinal());
		Fixture f = b.m_fixtureList;
		while(f != null){
			cli.writeByte(10); //Another fixture
			if(f.getShape() instanceof CircleShape){
				cli.writeByte(20); //Circle
				CircleShape cs = (CircleShape)f.getShape();
				writeVec(cli, cs.m_p);
				cli.writeFloat(cs.m_radius);
			} else {
				cli.writeByte(30); //Polygon
				PolygonShape ps = (PolygonShape)f.getShape();
				cli.writeInt(ps.m_vertexCount);
				for(int i=0; i<ps.m_vertexCount; i++){
					writeVec(cli, ps.m_vertices[i]);
				}
			}
			f = f.m_next;
		}
		cli.writeByte(40); //End of fixture list
	}
	
	void writeVec(DataOutputStream cli, Vec2 p) throws IOException{
		cli.writeFloat(p.x);
		cli.writeFloat(p.y);
	}

	void readInit(DataInputStream cli, Player player) throws IOException {
		_currPlayer = player;
		
		synchronized(_world){
			int newBodyCount = cli.readInt();

			for(int newBodyI = 0; newBodyI < newBodyCount; newBodyI++){
				addBody(cli.readInt(), readNewBody(cli));
			}
			int deadBodyCount = cli.readInt();
			for (int i = 0; i < deadBodyCount; i++) {
				deadBodies.add(bodies.get(cli.readInt()));
			}
		}
		_currPlayer.b = bodies.get(cli.readInt());
	}
	
	void readWorld(DataInputStream cli) throws IOException {
		int check = cli.readInt();
		if(check!=1337)
			throw new RuntimeException("Bad data feed: "+check);
		
		synchronized(_world){
			int newBodyCount = cli.readInt();
			for(int newBodyI = 0; newBodyI < newBodyCount; newBodyI++){
				addBody(cli.readInt(), readNewBody(cli));
			}
			
			int goneBodyCount = cli.readInt();
			for(int goneBodyI = 0; goneBodyI < goneBodyCount; goneBodyI++){
				removeBody(bodies.get(cli.readInt()));
			}

			int newDeadBodyCount = cli.readInt();
			for(int newDeadBodyI = 0; newDeadBodyI < newDeadBodyCount; newDeadBodyI++){
				deadBodies.add(bodies.get(cli.readInt()));
			}
			
			for(int modBodyI = 0; modBodyI < bodies.size(); modBodyI++){
				int id = cli.readInt();
				Body b = bodies.get(id);
				b.setTransform(readVec(cli), cli.readFloat());
				b.setLinearVelocity(readVec(cli));
				b.setAngularVelocity(cli.readFloat());
			}
		}
	}
	
	Body readNewBody(DataInputStream cli) throws IOException {
		int bodyType = cli.readInt();
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.values()[bodyType];
		bodyDef.position = new Vec2(0, 0);
		Body b = _world.createBody(bodyDef);
		
		while(cli.readByte()==10){//Another fixture
			byte shapeType = cli.readByte();
			
			if(shapeType == 20){
				CircleShape shape = new CircleShape();
				shape.m_p.set(readVec(cli));
				shape.m_radius = cli.readFloat();
				
				b.createFixture(shape, 0.1f);
				b.m_fixtureList.m_friction = 1f;
				
			} else if(shapeType == 30){
				PolygonShape shape = new PolygonShape();
				int numVerts = cli.readInt();
				Vec2[] verts = new Vec2[numVerts];
				for(int i=0;i<numVerts;i++)
					verts[i] = readVec(cli);
				shape.set(verts, numVerts);
				
				b.createFixture(shape, 0.1f);
				b.m_fixtureList.m_friction = 1f;
				
			} else {
				throw new RuntimeException("Received shape type "+shapeType);
			}
		}
		
		return b;
	}

	Vec2 readVec(DataInputStream cli) throws IOException{
		return new Vec2(cli.readFloat(), cli.readFloat());
	}
	
	void draw(Graphics2D g, int W, int H){
		Resources.transform(g);
	    g.addRenderingHints( new RenderingHints(
	             RenderingHints.KEY_TEXT_ANTIALIASING,
	             RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
	    g.addRenderingHints( new RenderingHints(
	             RenderingHints.KEY_ANTIALIASING,
	             RenderingHints.VALUE_ANTIALIAS_ON));
	    g.addRenderingHints( new RenderingHints(
	             RenderingHints.KEY_RENDERING,
	             RenderingHints.VALUE_RENDER_QUALITY));
	    g.addRenderingHints( new RenderingHints(
	             RenderingHints.KEY_INTERPOLATION,
	             RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, W, H);
		
		g.setColor(Color.BLACK);
		
		AffineTransform trans;
		g.translate(W/2, H/2);
		g.scale(scale, -scale);
		
		float mapW = 65;
		float screenW = W / scale;
		if(screenW < mapW){
			float leftover = (screenW - mapW);
			g.translate(leftover*(_currPlayer.b.getPosition().x)/mapW, 0);
		}
		
		float mapH = 34;
		float screenH = H / scale;
		if(screenH < mapH){
			float leftover = (screenH - mapH);
			g.translate(0, leftover*(_currPlayer.b.getPosition().y)/mapH);
		}
		
		trans = g.getTransform();
		
		synchronized(_world){
			for(Body b : bodies.values()){
				g.translate(b.getPosition().x, b.getPosition().y);
				g.rotate(b.getAngle());
				for(Fixture f = b.getFixtureList(); f != null; f = f.getNext()){
					Shape drawShape;
					
					if(f.getShape() instanceof CircleShape){
						Vec2 center = ((CircleShape)f.getShape()).m_p;
						float r = f.getShape().m_radius;
						
						drawShape = new Ellipse2D.Double(center.x - r, center.y - r, 2*r, 2*r);
						
					} else if(f.getShape() instanceof PolygonShape) {
						PolygonShape ps = (PolygonShape)f.getShape();
						
						Path2D.Float outline = new Path2D.Float();
						outline.moveTo(ps.m_vertices[0].x, ps.m_vertices[0].y);
						for(int i=0; i<ps.m_vertexCount; i++){
							Vec2 p = ps.m_vertices[i];
							outline.lineTo(p.x, p.y);
						}
						outline.closePath();
						
						drawShape = outline;
						
					} else {
						drawShape = null;
					}
					
					if (deadBodies.contains(b)) {
						g.setColor(Color.RED);
					} else if(b == _currPlayer.b) {
						g.setColor(Color.BLUE);
					} else {
						g.setColor(Color.BLACK);
					}

					g.fill(drawShape);
				}
				g.setTransform(trans);
			}
		}

	}

	void doJump(Body player) {
		Vec2 normSum = new Vec2(0, 0);
		int tacts = 0;
		for (ContactEdge ce = player.getContactList(); ce != null; ce = ce.next) {
			if (ce.contact.isTouching()) {
				tacts++;
				WorldManifold wm = getWorldManifold(ce.contact);
				Vec2 norm = wm.normal.clone();
				normSum.addLocal(norm);
			}
		}
		if (tacts == 0)
			return;
		normSum.normalize();
		player.applyLinearImpulse(normSum.mul(2.5f), player.getPosition());
		normSum.negateLocal();
		for (ContactEdge ce = player.getContactList(); ce != null; ce = ce.next) {
			if (ce.contact.isTouching()) {
				Body other = ce.other;
				WorldManifold wm = getWorldManifold(ce.contact);
//				Vec2 norm = wm.normal.clone();
				other.applyLinearImpulse(normSum.mul(2.5f / tacts),
						wm.points[0]);
			}
		}
	}

	static WorldManifold getWorldManifold(Contact c) {
		WorldManifold wm = new WorldManifold();
		Fixture fix1 = c.getFixtureA(), fix2 = c.getFixtureB();
		wm.initialize(c.getManifold(), fix1.getBody().getTransform(),
				fix1.getShape().m_radius, fix2.getBody().getTransform(),
				fix2.getShape().m_radius);
		return wm;
	}

	void createPlayer(Player player) {
		synchronized(_world){
			BodyDef playerDef = new BodyDef();
			playerDef.type = BodyType.DYNAMIC;
			playerDef.position = new Vec2(0, 10f);
			player.b = _world.createBody(playerDef);
			
			PolygonShape playerShape = new PolygonShape();
			playerShape.setAsBox(0.7f, 1.5f);
			player.b.createFixture(playerShape, 0.1f);
			player.b.m_fixtureList.m_friction = 1f;
			
			addBody(player.b);
		}
	}
}
