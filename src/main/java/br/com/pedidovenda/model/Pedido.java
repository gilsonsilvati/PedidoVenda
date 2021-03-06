package br.com.pedidovenda.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
@Table(name = "pedido")
public class Pedido implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Date dataCriacao;
	private String observacao;
	private Date dataEntrega;
	private BigDecimal valorFrete = BigDecimal.ZERO;
	private BigDecimal valorDesconto = BigDecimal.ZERO;
	private BigDecimal valorTotal = BigDecimal.ZERO;
	
	private StatusPedido status = StatusPedido.ORCAMENTO;
	private FormaPagamento formaPagamento;
	
	private Usuario vendedor;
	private Cliente cliente;
	private EnderecoEntrega enderecoEntrega;
	private List<ItemPedido> itens = new ArrayList<>();
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_criacao", nullable = false)
	public Date getDataCriacao() {
		return dataCriacao;
	}
	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}
	
	@Column(columnDefinition = "text")
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	
	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "data_entrega", nullable = false)
	public Date getDataEntrega() {
		return dataEntrega;
	}
	public void setDataEntrega(Date dataEntrega) {
		this.dataEntrega = dataEntrega;
	}
	
	@NotNull
	@Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
	public BigDecimal getValorFrete() {
		return valorFrete;
	}
	public void setValorFrete(BigDecimal valorFrete) {
		this.valorFrete = valorFrete;
	}
	
	@NotNull
	@Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
	public BigDecimal getValorDesconto() {
		return valorDesconto;
	}
	public void setValorDesconto(BigDecimal valorDesconto) {
		this.valorDesconto = valorDesconto;
	}
	
	@NotNull
	@Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
	public BigDecimal getValorTotal() {
		return valorTotal;
	}
	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	public StatusPedido getStatus() {
		return status;
	}
	public void setStatus(StatusPedido status) {
		this.status = status;
	}
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "forma_pagamento", nullable = false, length = 20)
	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}
	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "vendedor_id", nullable = false)
	public Usuario getVendedor() {
		return vendedor;
	}
	public void setVendedor(Usuario vendedor) {
		this.vendedor = vendedor;
	}
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "cliente_id", nullable = false)
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	@Embedded
	public EnderecoEntrega getEnderecoEntrega() {
		return enderecoEntrega;
	}
	public void setEnderecoEntrega(EnderecoEntrega enderecoEntrega) {
		this.enderecoEntrega = enderecoEntrega;
	}
	
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	public List<ItemPedido> getItens() {
		return itens;
	}
	public void setItens(List<ItemPedido> itens) {
		this.itens = itens;
	}
	
	@Transient
	public boolean isNovo() {
		return getId() == null;
	}
	
	@Transient
	public boolean isExistente() {
		return !isNovo();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		Pedido other = (Pedido) obj;
		return new EqualsBuilder().append(id, other.id).isEquals();
	}
	
	@Transient
	public BigDecimal getValorSubtotal() {
		return getValorTotal().subtract(getValorFrete()).add(getValorDesconto());
	}
	
	public void recalcularValorTotal() {
		BigDecimal total = BigDecimal.ZERO;
		
		total = total.add(getValorFrete()).subtract(getValorDesconto());
		
		for (ItemPedido item : getItens()) {
			if (item.getProduto() != null && item.getProduto().getId() != null) {
				total = total.add(item.getValorTotal());
			}
		}
		
		setValorTotal(total);
	}
	
	public void adicionarItemVazio() {
		if (isOrcamento()) {
			Produto produto = new Produto();
			
			ItemPedido item = new ItemPedido();
			item.setProduto(produto);
			item.setPedido(this);
			
			getItens().add(0, item);
		}
	}
	
	@Transient
	public boolean isOrcamento() {
		return StatusPedido.ORCAMENTO.equals(getStatus());
	}
	
	public void removerItemVazio() {
		ItemPedido primeiroItem = getItens().get(0);
		
		if (primeiroItem != null && primeiroItem.getProduto().getId() == null) {
			getItens().remove(0);
		}
	}
	
	@Transient
	public boolean isValorTotalNegativo() {
		return getValorTotal().compareTo(BigDecimal.ZERO) < 0;
	}
	
	@Transient
	public boolean isEmitido() {
		return StatusPedido.EMITIDO.equals(getStatus());
	}
	
	@Transient
	public boolean isNaoEmissivel() {
		return !isEmissivel();
	}
	
	@Transient
	public boolean isEmissivel() {
		return isExistente() && isOrcamento();
	}
	
	@Transient
	public boolean isNaoCancelavel() {
		return !isCancelavel();
	}
	
	@Transient
	public boolean isCancelavel() {
		return isExistente() && isNaoCancelado();
	}
	
	@Transient
	private boolean isNaoCancelado() {
		return !isCancelado();
	}
	@Transient
	private boolean isCancelado() {
		return StatusPedido.CANCELADO.equals(getStatus());
	}
	
	@Transient
	public boolean isNaoAlteravel() {
		return !isAlteravel();
	}
	
	@Transient
	public boolean isAlteravel() {
		return isOrcamento();
	}
	
	@Transient
	public boolean isNaoEnviavelPorEmail() {
		return isNovo() || isCancelado();
	}

}
