package com.proton.module;

public class ServiceName {
  private final String name;

  private ServiceName(String name) {
    this.name = name;
  }

  public static final ServiceName of(String name) {
    return new ServiceName(name);
  }

  public String getPath() {
    return name
      .replace(" ", "")
      .replace("/", "")
      .replace("\\", "")
      .toLowerCase();
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ServiceName that = (ServiceName) o;

    return name.equals(that.name);

  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
