import datetime
from dataclasses import dataclass, field
from typing import List

# Basado en los requisitos del SENIAT.
# El RIF (Registro Único de Información Fiscal) es el ID fiscal de Venezuela.
# Para personas naturales, puede ser la Cédula de Identidad o Pasaporte.

@dataclass
class EmpresaEmisora:
    """Representa la empresa que emite la factura."""
    nombre: str
    rif: str
    domicilio_fiscal: str

@dataclass
class Cliente:
    """Representa al cliente que recibe la factura."""
    nombre: str
    identificacion: str  # Puede ser RIF, Cédula o Pasaporte.
    domicilio_fiscal: str
    telefono: str = ""  # Opcional
    email: str = ""      # Opcional

@dataclass
class Producto:
    """Representa una línea de producto en la factura."""
    descripcion: str
    cantidad: float
    precio_unitario: float
    alicuota_iva: float  # La tasa de impuesto como porcentaje (ej. 16.0 para 16%)

    @property
    def precio_total(self) -> float:
        """Calcula el precio total para esta línea (cantidad * precio_unitario)."""
        return self.cantidad * self.precio_unitario

@dataclass
class Factura:
    """Representa una factura completa."""
    emisor: EmpresaEmisora
    cliente: Cliente
    productos: List[Producto]
    numero_control: str

    # Se usa default_factory para generar la fecha y hora actual con el formato requerido.
    fecha_emision: str = field(default_factory=lambda: datetime.datetime.now().strftime('%d%m%Y'))
    hora_emision: str = field(default_factory=lambda: datetime.datetime.now().strftime('%H.%M.%S'))

    # Datos de la imprenta digital, como lo exige la normativa.
    imprenta_nombre: str = "Imprenta Digital Autorizada, C.A."
    imprenta_rif: str = "J-12345678-9"
    imprenta_autorizacion: str = "SENIAT-PROV-2024-0001"

    @property
    def base_imponible(self) -> float:
        """Calcula el monto total sobre el cual se aplica el impuesto."""
        return sum(p.precio_total for p in self.productos)

    @property
    def total_iva(self) -> float:
        """Calcula el monto total del IVA."""
        return sum(p.precio_total * (p.alicuota_iva / 100.0) for p in self.productos)

    @property
    def monto_total(self) -> float:
        """Calcula el monto final y total de la factura."""
        return self.base_imponible + self.total_iva
