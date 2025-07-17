package com.example.backend.contact.contollerContact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.contact.dtoContact.ContactRequest;
import com.example.backend.contact.serviceContact.ContactService;

import io.swagger.v3.oas.annotations.Operation; // Для Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Для Swagger

@RestController
@Tag(name = "Контактная форма", description = "API для отправки данных из контактной формы") // Для Swagger
public class ContactController {

    @Autowired
    private ContactService contactService; // Инжектируем сервис

    @PostMapping("/api/send-email")
@CrossOrigin(
    origins = {
        "https://agrofarm.kz",
        "https://www.agrofarm.kz",
        "https://user.agrofarm.kz",
        "https://www.user.agrofarm.kz"
    },
    allowCredentials = "true"
)
    @Operation(summary = "Отправить контактные данные", description = "Отправляет данные (номер/email) пользователя на указанный почтовый адрес.") // Для Swagger
    public ResponseEntity<String> sendEmail(@RequestBody ContactRequest request) {
        try {
            contactService.sendContactEmail(request.getContactInfo());
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }
}
