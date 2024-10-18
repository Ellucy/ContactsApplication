package io.application.contactapi.service;

import io.application.contactapi.domain.Contact;
import io.application.contactapi.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.application.contactapi.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class ContactService {

    private final ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Contact is not found"));
    }

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public void deleteContact(String id) {
        Contact contact = getContact(id);
        contactRepository.deleteById(id);

        // Delete the photo file if it exists
        try {
            String photoUrl = contact.getPhotoUrl();
            if (photoUrl != null) {
                String fileName = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(PHOTO_DIRECTORY).resolve(fileName).toAbsolutePath().normalize();
                Files.deleteIfExists(filePath);  // Delete the file
                log.info("Deleted photo for user ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error while deleting photo for user ID: {}", id, e);
        }
    }

    public String uploadPhoto(String id, MultipartFile file) {
        log.info("Saving picture for user ID: {}", id);
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id, file);

        contact.setPhotoUrl(photoUrl);
        contactRepository.save(contact);
        return photoUrl;
    }


    private final Function<String, String> fileExtension = fileName ->
            Optional.of(fileName)
                    .filter(name -> name.contains("."))
                    .map(name -> name.substring(fileName.lastIndexOf(".") + 1))
                    .filter(ext -> ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg"))
                    .orElse(".png");


    // Takes in String and MultipartFile and is going to return a string
    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String extension = fileExtension.apply(image.getOriginalFilename());
        String fileName = id + "." + extension;

        try {
            // Get a specific location on computer
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();

            // If it doesn't exist, create it
            // When trying to save a file in the location that doesn't exist, an error is thrown
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(fileName), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/" + fileName)
                    .toUriString();
        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image", exception);
        }
    };
}
