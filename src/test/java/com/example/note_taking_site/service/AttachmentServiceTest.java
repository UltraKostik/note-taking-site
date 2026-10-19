package com.example.note_taking_site.service;

import com.example.note_taking_site.model.Attachment;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.AttachmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    private Notes note(Long id) {
        User author = new User();
        author.setId(1L);
        author.setEmail("author@example.com");
        Notes n = new Notes();
        n.setId(id);
        n.setTitle("Заметка");
        n.setAuthor(author);
        return n;
    }

    @Test
    void save_storesFileNameContentTypeSizeAndData() throws Exception {
        Notes note = note(1L);
        byte[] data = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", data);

        attachmentService.save(file, note);

        ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(captor.capture());
        Attachment saved = captor.getValue();
        assertThat(saved.getFileName()).isEqualTo("doc.txt");
        assertThat(saved.getContentType()).isEqualTo("text/plain");
        assertThat(saved.getSize()).isEqualTo(5L);
        assertThat(saved.getData()).isEqualTo(data);
        assertThat(saved.getNote()).isSameAs(note);
    }

    @Test
    void save_nullOriginalFilename_fallsBackToDefault() throws Exception {
        Notes note = note(1L);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getSize()).thenReturn(0L);
        when(file.getBytes()).thenReturn(new byte[0]);

        attachmentService.save(file, note);

        ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getFileName()).isEqualTo("file");
    }

    @Test
    void getById_foreignNote_throws() {
        Notes note = note(1L);
        when(attachmentRepository.findByIdAndNote(4L, note)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getById(4L, note))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_removesAttachment() {
        Notes note = note(1L);
        Attachment attachment = new Attachment();
        attachment.setId(4L);
        attachment.setNote(note);
        when(attachmentRepository.findByIdAndNote(4L, note)).thenReturn(Optional.of(attachment));

        attachmentService.delete(4L, note);

        verify(attachmentRepository).delete(attachment);
    }
}
