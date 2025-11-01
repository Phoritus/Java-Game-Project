## Aiden Adventure Game

เกมแอ็กชันผจญภัยสไตล์พิกเซลบน Java (AWT/Swing) ที่มีระบบสถานะเกมครบ เครื่องมือจัดการเหตุการณ์ (Event) การสลับฉาก/เทเลพอร์ต ระบบกลางวัน-กลางคืน พร้อม UI สำหรับไตเติล เมนู ตัวละคร อินเวนทอรี และบทสนทนาแบบพิมพ์ทีละตัวอักษร

### จุดเด่น (Highlights)
- ทรัพยากรภาพ/เสียงจัดเป็นระบบ: โฟลเดอร์ `res/` แยก tiles, objects, NPC/monster, projectile, sounds, fonts ชัดเจน รองรับการเพิ่มแผนที่/อนิเมชันได้ง่าย
- ระบบเกมที่หลากหลายพร้อมใช้งาน: State machine ของเกม (title/play/pause/dialogue/cutscene/transition/option/trade/sleep/game over), Event/Teleport/Healing, Lighting/Environment (วัน-คืน), Inventory/Trade/UI ครบถ้วน

---

## โครงสร้างโปรเจกต์ (สำคัญที่ควรรู้)
- `src/`
	- `main/`: แกนระบบเกม
		- `Main.java` จุดเริ่มโปรแกรม (สร้าง JFrame, แนบ GamePanel, โหลด config)
		- `GamePanel.java` ลูปเกม เรนเดอร์แบบดับเบิลบัฟเฟอร์ จัดการ state และระบบย่อย (UI/Env/Assets/Collision/Event)
		- `UI.java` วาดหน้าจอทุกสถานะ (Title, HUD, Dialogue, Inventory, Options, Trade, Transition ฯลฯ)
		- `KeyHandler.java` ปุ่มควบคุมและการนำทางเมนู
		- `EventHandler.java` + `EventRect.java` ระบบตรวจชน/ทริกเกอร์เหตุการณ์ (บ่อฮีล เทเลพอร์ต เปลี่ยนดันเจียน บอส ฯลฯ)
		- `CollisionChecker.java`, `AssetSetter.java`, `Sound.java`, `Config.java` เป็นต้น
	- `entity/`: เอนทิตีทั้งหมด (Player, NPC, Monster, Projectile, วัตถุที่หยิบได้ ฯลฯ)
	- `environment/`: แสง/สภาพแวดล้อม (`EnvironmentManager`, `Lighting`)
	- `tile/`, `tiles_interactive/`: ไทล์และไทล์โต้ตอบได้
- `res/`: แอสเซ็ตภาพ/เสียง/ฟอนต์/แผนที่ โหลดผ่าน classpath เช่น `getResource("/res/...")`
- `config.txt`: ไฟล์คอนฟิก 3 บรรทัด — 1) Fullscreen (On/Off) 2) Music volume (0-5) 3) SE volume (0-5)

---

## วิธีรัน

### 1) รันด้วย IDE (VS Code / IntelliJ / Eclipse)
1. เปิดโฟลเดอร์โปรเจกต์ แล้วใช้ Java Extension/Project Import ให้เห็นโครงสร้าง package
2. รันคลาส `src.main.Main` โดยตรง (Main method)

ข้อควรทราบ:
- แอสเซ็ตถูกอ่านผ่าน `getResource("/res/...")` ดังนั้นเมื่อรันจาก IDE ต้องให้โฟลเดอร์ `res/` อยู่บน classpath root (ปกติ IDE จะตั้งให้โดยคัดลอกหรือมาร์กเป็น resource)

---

## ปุ่มควบคุม (จาก `KeyHandler.java` และ `GamePanel.java`)
- เคลื่อนที่: W/A/S/D
- โจมตี: คลิกซ้ายเมาส์ (ระหว่าง play)
- โต้ตอบ/ยืนยัน/ใช้: F (เช่น บ่อฮีล, บทสนทนา), Enter (ยืนยันในเมนู)
- หยุด/เล่นต่อ: P
- ตัวเลือก/กลับ: Esc (เข้าสู่ Option หรือย้อนกลับจากเมนูย่อย)
- หน้าตัวละคร/อินเวนทอรี: C (ลูกศร/WASD เลื่อนช่อง, Enter ใช้ไอเท็ม)
- ดีบัก: F1 (แสดงค่า dev), F2 (โหลดแผนที่ตัวอย่าง), F3 (God mode), F4 (Boss debug บนแผนที่บอส)
- ไตเติลสกรีน: W/S เลือกเมนู, Enter ยืนยัน (NEW GAME / QUIT)

---

## ระบบหลักที่เกี่ยวข้อง
- Game States: `title`, `play`, `pause`, `dialogue`, `character`, `option`, `trade`, `transition`, `sleep`, `cutscene`, `gameOver`
- Event System: กำหนด `EventRect` เป็นกริดต่อแผนที่ ตรวจชนด้วย `EventHandler` เพื่อทริกเกอร์บ่อฮีล เทเลพอร์ต เปลี่ยนโซน/ดันเจียน บอส ฯลฯ พร้อมกลไกกันการทริกซ้ำใกล้ตำแหน่งเดิม
- Lighting/Environment: วงจรวัน-คืน ใส่เลเยอร์แสง/ความมืด และรีเซ็ตแสงเมื่อเริ่มเกมใหม่
- Asset/Collision: โหลดออบเจ็กต์/มอนสเตอร์/ไทล์ผ่าน `AssetSetter` และตรวจชนแบบ AABB ผ่าน `CollisionChecker`

---

## การตั้งค่า (`config.txt`)
ไฟล์นี้อ่าน/เขียนโดย `Config.java` มี 3 บรรทัดเรียงลำดับดังนี้:
1. โหมดหน้าจอ: `On` = Fullscreen, `Off` = Windowed
2. Music volume: ค่าจำนวนเต็ม 0–5
3. Sound Effect (SE) volume: ค่าจำนวนเต็ม 0–5

สามารถแก้ไขก่อนรัน หรือปรับบนหน้า Options แล้วเกมจะบันทึกกลับให้อัตโนมัติ

---

## เคล็ดลับ/ปัญหาที่พบบ่อย
- ภาพ/ทรัพยากรไม่ขึ้น: ตรวจว่า classpath มีโฟลเดอร์ `res/` ที่ราก (เวลารันต้องใส่ `.;` หรือคัดลอก `res/` เข้าโฟลเดอร์ที่ใช้รัน)
- หน้าจอมืดหลังจบฉาก/รีสตาร์ท: ระบบรีเซ็ตแสงถูกใส่ไว้ใน `GamePanel.resetGame()` แล้ว หากยังมืดให้ลองลบไฟล์ `config.txt` เพื่อรีเซ็ตค่า หรือทดสอบรันแบบ Windowed
- เสียงเงียบ: ตรวจ `config.txt` บรรทัด 2–3 (ค่า 0 คือปิดเสียง)

---

หากต้องการเอกสารเชิงลึก (สถาปัตยกรรม Entity, การทำงานของ Event/Lighting/UI, หรือแนวทางทำ Data‑Driven Events) แจ้งได้ ยินดีเพิ่มใน README หรือสร้างเอกสารแยกครับ

