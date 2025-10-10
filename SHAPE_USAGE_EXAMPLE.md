# Shape Utility Class - Usage Examples

## สิ่งที่ Shape.java มี:

### 1. Static Methods สำหรับสร้าง Shapes:

```java
// สร้าง circular Area (วงกลม)
Area circle = Shape.createCircleArea(centerX, centerY, diameter);

// สร้าง rectangular Area (สี่เหลี่ยม)
Area rect = Shape.createRectangleArea(x, y, width, height);

// สร้าง darkness overlay พร้อมแสงกลางวง
Area darkness = Shape.createDarknessWithLight(
    screenWidth, screenHeight,
    lightCenterX, lightCenterY,
    lightRadius
);
```

## ตัวอย่างการใช้งานใน Lighting.java:

### แบบเดิม (ไม่ใช้ Shape utility):
```java
Area screenArea = new Area(new Rectangle(0, 0, gp.screenWidth, gp.screenHeight));

double x = centerX - (circleSize / 2);
double y = centerY - (circleSize / 2);

Shape circleShape = new Ellipse2D.Double(x, y, circleSize, circleSize);
Area lightArea = new Area(circleShape);

screenArea.subtract(lightArea);
```

### แบบใหม่ (ใช้ Shape utility):
```java
// วิธีที่ 1: ใช้ helper method ครบชุด
Area screenArea = Shape.createDarknessWithLight(
    gp.screenWidth, gp.screenHeight,
    centerX, centerY,
    circleSize / 2  // radius
);

// หรือ วิธีที่ 2: สร้างทีละส่วน
Area screenArea = Shape.createRectangleArea(0, 0, gp.screenWidth, gp.screenHeight);
Area lightArea = Shape.createCircleArea(centerX, centerY, circleSize);
screenArea.subtract(lightArea);
```

## ประโยชน์ของ Shape utility class:

1. **Code ที่สั้นและอ่านง่ายขึ้น** - ไม่ต้องคำนวณ x, y offset เอง
2. **Reusable** - ใช้ซ้ำได้ในหลายที่ (Lighting, particle effects, collision shapes, etc.)
3. **Type-safe** - ใช้ java.awt.Shape interface อย่างถูกต้อง
4. **Maintainable** - แก้ไขที่เดียว ใช้ได้ทุกที่

## สิ่งที่ควรทราบ:

- `java.awt.Shape` คือ **interface** ใน Java AWT
- `Ellipse2D.Double`, `Rectangle2D.Double` implement Shape interface
- `Area` class ใช้สำหรับ boolean operations (subtract, intersect, union)
