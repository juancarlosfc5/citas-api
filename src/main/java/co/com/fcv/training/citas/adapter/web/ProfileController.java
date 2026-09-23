package co.com.fcv.training.citas.adapter.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/users")
class ProfileController {
    record PhoneRequest(@NotBlank @Size(max=40) String phone) {}
    private final JdbcTemplate jdbc;
    ProfileController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/me") @PreAuthorize("isAuthenticated()") Map<String,Object> me(Authentication a){return jdbc.query("select id,first_name as firstName,last_name as lastName,document_type as documentType,document_number as documentNumber,email,phone from users where id=?",rs->{if(!rs.next())throw new IllegalArgumentException("Usuario no encontrado");Map<String,Object> m=new LinkedHashMap<>(); for(int i=1;i<=rs.getMetaData().getColumnCount();i++)m.put(rs.getMetaData().getColumnLabel(i),rs.getObject(i));return m;},id(a));}
    @PatchMapping("/me") @PreAuthorize("isAuthenticated()") Map<String,Object> patch(Authentication a,@Valid @RequestBody PhoneRequest p){jdbc.update("update users set phone=? where id=?",p.phone().trim(),id(a));return me(a);}
    private Long id(Authentication a){return Long.valueOf(a.getName());}
}
