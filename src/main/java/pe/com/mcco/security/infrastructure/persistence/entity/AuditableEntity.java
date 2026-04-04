package pe.com.mcco.security.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase base para entidades con columnas de auditoria y eliminacion logica.
 * <p>
 * Columnas: usr_creacion, fec_creacion, usr_modificacion, fec_modificacion, estado.
 * Estado: 'A' = Activo, 'I' = Inactivo (eliminado logicamente).
 * <p>
 * Implementa control de isNew via flag transitorio para que Spring Data
 * distinga INSERT de UPDATE: en INSERT solo puebla fec_creacion/usr_creacion,
 * en UPDATE solo puebla fec_modificacion/usr_modificacion.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditableEntity implements Persistable<@NonNull UUID> {

    public static final String ACTIVO = "A";
    public static final String INACTIVO = "I";

    @CreatedBy
    @Column(name = "usr_creacion", nullable = false, updatable = false)
    private String usrCreacion;

    @CreatedDate
    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @LastModifiedBy
    @Column(name = "usr_modificacion", insertable = false)
    private String usrModificacion;

    @LastModifiedDate
    @Column(name = "fec_modificacion", insertable = false)
    private LocalDateTime fecModificacion;

    @Column(name = "estado", nullable = false, length = 1)
    private String estado;

    /**
     * Flag transitorio para indicar a Spring Data que la entidad es nueva.
     * Necesario porque el UUID se asigna manualmente antes del persist,
     * y sin este flag Spring Data asume que es un UPDATE (ID no null).
     */
    @Transient
    @Builder.Default
    private boolean nuevo = true;

    @Override
    public boolean isNew() {
        return this.nuevo;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.nuevo = false;
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.estado == null) {
            this.estado = ACTIVO;
        }
    }

    public boolean isActivo() {
        return ACTIVO.equals(this.estado);
    }

    public void desactivar() {
        this.estado = INACTIVO;
    }

    public void activar() {
        this.estado = ACTIVO;
    }
}
