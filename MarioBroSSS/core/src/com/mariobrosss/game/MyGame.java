package com.mariobrosss.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import actores.Bala;
import actores.Cannon;
import actores.Goomba;
import actores.Mario;
import actores.Suelo;
import utiles.Constantes;
import utiles.MetricSize;
import utiles.MetricVector2;

public class MyGame {

	private static final float FactorZoomCamera = 1;

	private TmxMapLoader maploader;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;

	World world;
	Stage stage;
	SpriteBatch batch;
	InputAdapter input;
	GameCamera camera;
	Movimiento movimiento;
	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	Suelo suelo, suelo2, suelo3;
	Mario mario;
	Bala bala;
	Goomba goomba;
	InputAdapter prueba;
	Music music;
	InputMultiplexer multiplexor;
	Cannon cannonUno, cannonDos;
	boolean pausa = false;
	ArrayList<Bala> listaBalas = new ArrayList<Bala>();
	int contador = 0;
	public MyGame() {
		super();
		world = new World(new Vector2(Constantes.GRAVEDAD_X, Constantes.GRAVEDAD_Y), true);
		maploader = new TmxMapLoader();
		map = maploader.load("level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map);
		new B2WorldCreator(world, map);
		music = Gdx.audio.newMusic(Gdx.files.internal("marioTheme.mp3"));
		batch = new SpriteBatch();
		debugRenderer = new Box2DDebugRenderer();
		stage = new Stage();
		// mario = new Mario(new MetricVector2(100, 20), world, new MetricSize(10, 16));
		mario = new Mario(new MetricVector2(100, 100), world, new MetricSize(10, 16));
		cannonUno = new Cannon(new MetricVector2(34 * Constantes.PIXELS_TO_METERS, 0.56f * Constantes.PIXELS_TO_METERS),
				world, new MetricSize(20, 40));
		camera = new GameCamera();
		movimiento = new Movimiento(mario);
		// suelo = new Suelo(new MetricVector2(1692f, -256f), new MetricSize(2048, 64),
		// world);
		// suelo2 = new Suelo(new MetricVector2(0f, 0), new MetricSize(64, 48), world);
		// suelo2 = new Suelo(new MetricVector2(512f, 120f), new MetricSize(64, 48),
		// world);
		// bala = new Bala(new MetricVector2(50, 130), world, new MetricSize(40, 30));
		goomba = new Goomba(new MetricVector2(50, 20), world, new MetricSize(10, 10));
		stage.addActor(goomba);
		// stage.addActor(bala);
		bala = new Bala(new MetricVector2(34 * Constantes.PIXELS_TO_METERS, 0.56f * Constantes.PIXELS_TO_METERS), world,
				new MetricSize(20, 15));
		listaBalas.add(bala);
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
		    @Override
		    public void run() {
		       createBullet(); 
		    }
		}, 2, 2, TimeUnit.SECONDS);
//		stage.addActor(bala);
		stage.addActor(cannonUno);
		stage.addActor(mario);
		multiplexor = new InputMultiplexer();
		multiplexor.addProcessor(0, camera);
		multiplexor.addProcessor(1, movimiento);
		Gdx.input.setInputProcessor(multiplexor);
		world.setContactListener(new ListenerSalto(mario));
		world.setContactListener(new ListenerBalaDrop(mario, bala));
		movimiento.setCamera(camera);
		camera.setMario(mario);
		music.play();
		music.setLooping(true);
		music.setVolume(0.9f);
		mario.setMovimiento(movimiento);
		// camera.camera.position.set(stage.getViewport().getScreenWidth()/2,
		// stage.getViewport().getWorldHeight()/4.2f,camera.camera.position.z);

	}

	public void createBullet() {
		listaBalas.add(new Bala(new MetricVector2(34 * Constantes.PIXELS_TO_METERS, 0.56f * Constantes.PIXELS_TO_METERS), world,
				new MetricSize(20, 15)));
		for (Bala bala : listaBalas) {
			System.out.println(bala.toString());
		}
	}

	public void act() {
		contador++;
		if (contador == 90) {
			createBullet();
			contador = 0;
		}
		if (!pausa) {
			world.step(1f / 60f, 6, 2);
			for (Bala bala : listaBalas) {
				bala.act(Gdx.graphics.getDeltaTime());
			}
			stage.act();
			if (bala.isSetForDrop()) {
				bala.drop();
			}
		}
	}

	public void render() {
		this.act();
		stage.act();
		renderer.setView(camera.camera);
		camera.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		batch.setProjectionMatrix(camera.combined());
		debugMatrix = batch.getProjectionMatrix().cpy().scale(Constantes.PIXELS_TO_METERS, Constantes.PIXELS_TO_METERS,
				0);
		renderer.render();
		for (Iterator iterator = listaBalas.iterator(); iterator.hasNext();) {
			Bala bala = (Bala) iterator.next();
			bala.update();
			if (bala.isDead())
				iterator.remove();
		}
		batch.begin();
		mario.draw(batch);
		goomba.draw(batch);
		cannonUno.draw(batch);
		for (Bala bala : listaBalas) {
			if (!bala.isDead())
				bala.draw(batch);
		}
		batch.end();
		debugRenderer.render(world, debugMatrix);
	}

	public void dispose() {
		world.dispose();
		music.dispose();
		batch.dispose();
		stage.dispose();

	}
}