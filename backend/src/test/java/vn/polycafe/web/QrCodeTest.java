package vn.polycafe.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.AuditService;

class QrCodeTest {
 @Test void requestKeyFitsDatabaseAndSeparatesMembers(){
  UUID user=UUID.randomUUID(),request=UUID.randomUUID();
  String key=QrCustomerController.requestKey(user,request);
  assertEquals(64,key.length());assertEquals(key,QrCustomerController.requestKey(user,request));
  assertNotEquals(key,QrCustomerController.requestKey(UUID.randomUUID(),request));
 }
 @Test void qrPixelsRoundTripAndUnsafeBaseUrlsAreRejected()throws Exception{
  var db=mock(JdbcTemplate.class);UUID actor=UUID.randomUUID(),table=UUID.randomUUID();
  when(db.queryForList(anyString(),eq(false),eq(actor))).thenReturn(List.of(Map.of("id",table,"qr_token","opaque-token-123")));
  var audit=mock(AuditService.class);var json=new ObjectMapper();
  var controller=new QrCustomerController(db,audit,json,"https://cafe.example/polycafe");
  String svg=controller.image(table,new StaffUser(actor,"staff-test","Nhân viên",0,Set.of("staff"),false));
  int[] pixels=new int[256*256];Arrays.fill(pixels,0xffffff);
  var matcher=Pattern.compile("M(\\d+) (\\d+)h1v1h-1z").matcher(svg);
  while(matcher.find())pixels[Integer.parseInt(matcher.group(2))*256+Integer.parseInt(matcher.group(1))]=0;
  var source=new RGBLuminanceSource(256,256,pixels);
  var result=new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(source)));
  assertEquals("https://cafe.example/polycafe/qr/opaque-token-123",result.getText());
  assertThrows(IllegalArgumentException.class,()->new QrCustomerController(db,audit,json,"javascript:alert(1)"));
  assertThrows(IllegalArgumentException.class,()->new QrCustomerController(db,audit,json,"https://user:password@cafe.example"));
 }
}
