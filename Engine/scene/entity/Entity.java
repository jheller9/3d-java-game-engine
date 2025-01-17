package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import geom.AxisAlignedBBox;
import gl.Window;
import gl.anim.Animator;
import gl.res.Model;
import gl.res.Texture;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import util.Colors;

public abstract class Entity {
	private static final AxisAlignedBBox NO_BOUNDINGBOX = new AxisAlignedBBox(Vector3f.ZERO, Vector3f.ZERO);
	
	public Vector3f pos = new Vector3f();
	public Vector3f rot = new Vector3f();
	private Matrix4f mat = new Matrix4f();
	public float scale = 1f;
	
	public boolean visible = true;
	
	private Model model;
	protected Texture texture;
	private Animator animator;
	
	protected String name;
	
	public boolean deactivated;
	public Vector3f[] lighting = new Vector3f[6];
	
	protected BspLeaf leaf;

	protected AxisAlignedBBox bbox = NO_BOUNDINGBOX;
	
	protected float deactivationRange = Float.POSITIVE_INFINITY;
	
	private Vector3f color = new Vector3f(1f, 1f, 1f);
	private float colorBlendFactor = 0f;
	
	public void setLeaf(BspLeaf leaf) {
		this.leaf = leaf;
	}

	public Entity(String name) {
		this.name = name;
		for(int i = 0; i < 6; i++) {
			lighting[i] = new Vector3f(1,1,1);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setModel(Model model) {
		this.model = model;
		if (this.model == Resources.ERROR) {
			this.texture = Resources.getTexture("error");
		}
	}
	
	public void setModel(String model) {
		this.model = Resources.getModel(model);
		if (this.model == Resources.ERROR) {
			this.texture = Resources.getTexture("error");
		}
	}

	public void setTexture(Texture texture) {
		if (this.model == Resources.ERROR)
			return;
		this.texture = texture;
	}

	public void setTexture(String texture) {
		if (this.model == Resources.ERROR)
			return;
		this.texture = Resources.getTexture(texture);
	}

	public void setAnimator(Animator animator) {
		this.animator = animator;
	}

	public void update(PlayableScene scene) {
		mat.identity();
		mat.translate(pos);
		mat.rotate(rot);
		mat.scale(scale);
		Vector3f[] targetLight = scene.getArchitecture().getLightsAt(pos);
		for(int i = 0; i < 6; i++) {
			lighting[i] = Vector3f.lerp(targetLight[i], lighting[i], 10f * Window.deltaTime);
		}
		
		if (animator != null) 
			animator.update();
		
		if (this.model == Resources.ERROR) {
			this.setColor(Colors.alertColor());
		}
		
	}
	
	public Model getModel() {
		return model;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public Matrix4f getMatrix() {
		return mat;
	}
	
	public Animator getAnimator() {
		return animator;
	}

	public void cleanUp() {
		if (animator != null) {
			animator.destroy();
		}
	}

	public BspLeaf getLeaf() {
		return leaf;
	}

	public AxisAlignedBBox getBBox() {
		return bbox;
	}
	
	public Vector3f getColor() {
		return color;
	}

	public float getColorBlendFactor() {
		return colorBlendFactor;
	}
	
	public void setColor(Vector3f color) {
		this.setColor(color, 0.5f);
	}

	public void setColor(Vector3f color, float colorBlendFactor) {
		this.color = color;
		this.colorBlendFactor = colorBlendFactor;
	}
}
