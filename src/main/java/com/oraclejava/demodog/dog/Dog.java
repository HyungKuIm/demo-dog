package com.oraclejava.demodog.dog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dogs")
public class Dog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String kind;

	@Column(nullable = false)
	private Integer price;

	private String image;

	@Column(length = 100)
	private String country;

	private Double height;

	private Double weight;

	@Column(length = 4000)
	private String content;

	@Column(nullable = false)
	private int readcount;

	protected Dog() {
	}

	public Dog(DogRequest request) {
		apply(request);
	}

	public void apply(DogRequest request) {
		this.kind = request.kind();
		this.price = request.price();
		this.country = request.country();
		this.height = request.height();
		this.weight = request.weight();
		this.content = request.content();
	}

	public void changeImage(String image) {
		this.image = image;
	}

	public void increaseReadcount() {
		this.readcount++;
	}

	public Long getId() {
		return id;
	}

	public String getKind() {
		return kind;
	}

	public Integer getPrice() {
		return price;
	}

	public String getImage() {
		return image;
	}

	public String getCountry() {
		return country;
	}

	public Double getHeight() {
		return height;
	}

	public Double getWeight() {
		return weight;
	}

	public String getContent() {
		return content;
	}

	public int getReadcount() {
		return readcount;
	}

}
